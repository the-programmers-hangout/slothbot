package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.princesslana.smalld.SmallD;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

public class MessageCounter {

  private static final Duration BUCKET_DURATION = Duration.ofSeconds(10);
  private static final int PAST_BUCKETS_SIZE = 30;

  private final AtomicReference<CountingBucket> currentBucket =
      new AtomicReference<>(new CountingBucket());

  private final AtomicReference<ImmutableList<ImmutableBucket>> pastBuckets =
      new AtomicReference<>(ImmutableList.of());

  private final ScheduledExecutorService executor;

  public MessageCounter(ScheduledExecutorService executor) {
    this.executor = executor;
  }

  private void onGatewayPayload(String payload) {
    var json = Json.parse(payload).asObject();

    var isMessageCreate =
        json.getInt("op", -1) == 0 && json.getString("t", "").equals("MESSAGE_CREATE");

    if (isMessageCreate) {
      onMessageCreate(json.get("d").asObject());
    }
  }

  private void onMessageCreate(JsonObject d) {
    var channelId = d.getString("channel_id", null);

    if (channelId != null) {
      currentBucket.get().increment(channelId);
    }
  }

  private void rotateBucket() {
    var bucket = currentBucket.getAndSet(new CountingBucket()).snapshot();

    var rotated =
        Stream.concat(Stream.of(bucket), pastBuckets.get().stream().limit(PAST_BUCKETS_SIZE - 1))
            .collect(ImmutableList.toImmutableList());

    pastBuckets.set(rotated);
  }

  public ImmutableList<Rate> getBuckets(String channelId) {
    var counts = pastBuckets.get().stream().map(b -> b.getCount(channelId));

    return Stream.concat(counts, Stream.generate(() -> 0L))
        .limit(PAST_BUCKETS_SIZE)
        .map(c -> Rate.per(BUCKET_DURATION, c))
        .collect(ImmutableList.toImmutableList());
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
  }
}
