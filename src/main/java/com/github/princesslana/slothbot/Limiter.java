package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.WriterConfig;
import com.github.princesslana.jsonf.JsonF;
import com.github.princesslana.smalld.SmallD;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Limiter {

  private static final Logger LOG = LogManager.getLogger(Limiter.class);

  private final SmallD smalld;
  private final MessageCounter counter;
  private final ScheduledExecutorService executor;
  private final Path savePath;

  private final ConcurrentMap<Channel, Rate> limits = new ConcurrentHashMap<>();
  private final ConcurrentMap<Channel, Integer> slowmodes = new ConcurrentHashMap<>();

  public Limiter(
      SmallD smalld, MessageCounter counter, ScheduledExecutorService executor, Path savePath) {
    this.smalld = smalld;
    this.counter = counter;
    this.executor = executor;
    this.savePath = savePath;
  }

  private void updateSlowMode() {
    LOG.debug("Updating slow mode for {} channels...", limits.size());
    limits.keySet().forEach(c -> CompletableFuture.runAsync(() -> updateSlowMode(c), executor));
  }

  private void updateSlowMode(Channel channel) {
    var limit = limits.get(channel);

    var slowmode =
        (int)
            Streams.mapWithIndex(
                    counter.getBuckets(channel.getId()).stream(),
                    (b, idx) -> b.exceeds(limit) ? b.ratioTo(limit) * (30.0 - idx) / 10.0 : 0.0)
                .mapToDouble(n -> n)
                .sum();

    LOG.debug("Calculated slow mode for channel {} as {} seconds.", channel, slowmode);
    updateSlowMode(channel, slowmode);
  }

  private void updateSlowMode(Channel channel, int seconds) {
    if (slowmodes.containsKey(channel) && slowmodes.get(channel).intValue() == seconds) {
      LOG.debug("Slowmode for channel {} already {} seconds.", channel, seconds);
      return;
    }

    LOG.debug("Updating slowmode for for channel {} to {} seconds...", channel, seconds);
    smalld.patch(
        "/channels/" + channel.getId(),
        Json.object().add("rate_limit_per_user", seconds).toString());

    slowmodes.put(channel, seconds);
  }

  public void clear(Channel channel) {
    limits.remove(channel);
    updateSlowMode(channel, 0);
    save();
  }

  public void set(Channel channel, Rate limit) {
    limits.put(channel, limit);
    save();
  }

  public Optional<Rate> get(Channel channel) {
    return Optional.ofNullable(limits.get(channel));
  }

  public Map<Channel, Rate> getForGuild(String guildId) {
    return Maps.filterKeys(limits, c -> c.getGuildId().equals(guildId));
  }

  private void save() {
    var json = Json.array();

    limits.forEach(
        (k, v) -> json.add(Json.object().add("channel", k.toJson()).add("limit", v.toJson())));

    try {
      MoreFiles.asCharSink(savePath, Charsets.UTF_8)
          .write(json.toString(WriterConfig.PRETTY_PRINT));
      LOG.atDebug().log("Saved {} limits to {}", json.size(), savePath);
    } catch (IOException e) {
      LOG.atWarn().withThrowable(e).log("Error saving limits to {}", savePath);
    }
  }

  public void load() {
    try {
      var arr = JsonF.parse(MoreFiles.asCharSource(savePath, Charsets.UTF_8).read());

      for (var json : arr) {
        var channel = Channel.fromJson(json.get("channel"));
        var rate = Rate.fromJson(json.get("limit"));

        Optionals.ifPresent(channel, rate, limits::put);
      }

      LOG.atDebug().log("Loaded {} limits from {}", limits.size(), savePath);
    } catch (NoSuchFileException e) {
      LOG.atInfo().log("No limits file at {}. One will be created if needed.", savePath);
    } catch (IOException e) {
      LOG.atWarn().withThrowable(e).log("Error loading limits from {}", savePath);
    }
  }

  public void start() {
    executor.scheduleAtFixedRate(this::updateSlowMode, 10, 10, TimeUnit.SECONDS);
  }
}
