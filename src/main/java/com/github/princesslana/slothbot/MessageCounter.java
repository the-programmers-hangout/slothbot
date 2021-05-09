package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.WriterConfig;
import com.github.princesslana.jsonf.JsonF;
import com.github.princesslana.smalld.SmallD;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageCounter {

  private static final Logger LOG = LogManager.getLogger(MessageCounter.class);
  private static final Duration BUCKET_DURATION = Duration.ofSeconds(10);
  private static final int PAST_BUCKETS_SIZE = 30;

  private final AtomicReference<CountingBucket> currentBucket =
      new AtomicReference<>(new CountingBucket());

  private final AtomicReference<ImmutableList<ImmutableBucket>> pastBuckets =
      new AtomicReference<>(ImmutableList.of());

  private final ConcurrentHashMap<String, Boolean> botCounterConfigs = new ConcurrentHashMap<>();

  private final ScheduledExecutorService executor;
  private final Path savePath;

  public MessageCounter(ScheduledExecutorService executor, Path savePath) {
    this.executor = executor;
    this.savePath = savePath;
  }

  private void onGatewayPayload(String payload) {
    Discord.ifEvent(JsonF.parse(payload), "MESSAGE_CREATE", this::onMessageCreate);
  }

  private void onMessageCreate(JsonF d) {
    var isBotMessage =
        d.get("webhook_id").asString().isPresent()
            || d.get("author").get("bot").asBoolean().orElse(false);
    d.get("guild_id")
        .asString()
        .ifPresent(
            guildId -> {
              if (isCountingBotMessages(guildId) || !isBotMessage) {
                d.get("channel_id").asString().ifPresent(currentBucket.get()::increment);
              }
            });
  }

  private void rotateBucket() {
    var bucket = currentBucket.getAndSet(new CountingBucket()).snapshot();

    pastBuckets.updateAndGet(
        pbs ->
            Stream.concat(Stream.of(bucket), pbs.stream().limit(PAST_BUCKETS_SIZE - 1))
                .collect(ImmutableList.toImmutableList()));
  }

  public ImmutableList<Rate> getBuckets(Channel channel) {
    return getBuckets(channel.getId());
  }

  public ImmutableList<Rate> getBuckets(String channelId) {
    var counts = pastBuckets.get().stream().map(b -> b.getCount(channelId));

    return Stream.concat(counts, Stream.generate(() -> 0L))
        .limit(PAST_BUCKETS_SIZE)
        .map(c -> Rate.per(BUCKET_DURATION, c))
        .collect(ImmutableList.toImmutableList());
  }

  public ImmutableMap<String, Long> getTotalCounts() {
    return pastBuckets.get().stream()
        .flatMap(b -> b.getAllCounts().entrySet().stream())
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
  }

  public void clearBuckets(Channel channel) {
    clearBuckets(channel.getId());
  }

  public void clearBuckets(String channelId) {
    pastBuckets.updateAndGet(
        pbs ->
            pbs.stream()
                .map(b -> b.clearCount(channelId))
                .collect(ImmutableList.toImmutableList()));
  }

  public boolean isCountingBotMessages(String guildId) {
    return botCounterConfigs.getOrDefault(guildId, false);
  }

  public void setBotCounterConfig(String guildId, boolean value) {
    botCounterConfigs.put(guildId, value);
    save();
  }

  private void save() {
    var json = Json.array();

    botCounterConfigs.forEach(
        (k, v) -> json.add(Json.object().add("guild_id", k).add("count_bot_msgs", v)));

    try {
      MoreFiles.asCharSink(savePath, Charsets.UTF_8)
          .write(json.toString(WriterConfig.PRETTY_PRINT));
      LOG.atDebug().log("Saved {} counter configs to {}", json.size(), savePath);
    } catch (IOException e) {
      LOG.atWarn().withThrowable(e).log("Error saving counter configs to {}", savePath);
    }
  }

  public void load() {
    try {
      var arr = JsonF.parse(MoreFiles.asCharSource(savePath, Charsets.UTF_8).read());

      for (var json : arr) {
        var guild = json.get("guild_id").asString();
        var botCounterConfig = json.get("count_bot_msgs").asBoolean();

        Optionals.ifPresent(guild, botCounterConfig, botCounterConfigs::put);
      }

      LOG.atDebug().log("Loaded {} counter configs from {}", botCounterConfigs.size(), savePath);
    } catch (NoSuchFileException e) {
      LOG.atInfo().log("No counter configs file at {}. One will be created if needed.", savePath);
    } catch (IOException e) {
      LOG.atWarn().withThrowable(e).log("Error loading counter config from {}", savePath);
    }
  }

  public void start(SmallD smalld) {
    smalld.onGatewayPayload((p) -> CompletableFuture.runAsync(() -> onGatewayPayload(p), executor));

    executor.scheduleAtFixedRate(
        this::rotateBucket,
        BUCKET_DURATION.toSeconds(),
        BUCKET_DURATION.toSeconds(),
        TimeUnit.SECONDS);
  }

  private static class CountingBucket {
    private final ConcurrentMap<String, LongAdder> counts = new ConcurrentHashMap<>();

    public void increment(String channelId) {
      counts.computeIfAbsent(channelId, k -> new LongAdder()).increment();
    }

    public ImmutableBucket snapshot() {
      var snapshot =
          counts.entrySet().stream()
              .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> e.getValue().sum()));

      return new ImmutableBucket(snapshot);
    }
  }

  private static class ImmutableBucket {
    private final ImmutableMap<String, Long> counts;

    public ImmutableBucket(ImmutableMap<String, Long> counts) {
      this.counts = counts;
    }

    public Long getCount(String channelId) {
      return counts.getOrDefault(channelId, 0L);
    }

    public ImmutableMap<String, Long> getAllCounts() {
      return counts;
    }

    public ImmutableBucket clearCount(String channelId) {
      var mutable = new HashMap<>(counts);
      mutable.remove(channelId);
      return new ImmutableBucket(ImmutableMap.copyOf(mutable));
    }
  }
}
