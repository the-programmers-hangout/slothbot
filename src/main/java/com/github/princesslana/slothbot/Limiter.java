package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.github.princesslana.smalld.SmallD;
import com.google.common.collect.Streams;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Limiter {

  private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  private final SmallD smalld;
  private final MessageCounter counter;

  private final ConcurrentMap<String, Rate> limits = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Integer> slowmodes = new ConcurrentHashMap<>();

  public Limiter(SmallD smalld, MessageCounter counter) {
    this.smalld = smalld;
    this.counter = counter;
  }

  private void updateSlowMode() {
    limits.keySet().forEach(cid -> CompletableFuture.runAsync(() -> updateSlowMode(cid)));
  }

  private void updateSlowMode(String channelId) {
    var limit = limits.get(channelId);

    var slowmode =
        (int)
            Streams.mapWithIndex(
                    counter.getBuckets(channelId).stream(),
                    (b, idx) -> b.exceeds(limit) ? (30.0 - idx) / 10.0 : 0.0)
                .mapToDouble(n -> n)
                .sum();

    updateSlowMode(channelId, slowmode);
  }

  private void updateSlowMode(String channelId, int seconds) {
    if (slowmodes.get(channelId) != seconds) {
      smalld.patch(
          "/channels/" + channelId, Json.object().add("rate_limit_per_user", seconds).toString());
    }
  }

  public void clear(String channelId) {
    limits.remove(channelId);
    updateSlowMode(channelId, 0);
  }

  public void set(String channelId, Rate limit) {
    limits.put(channelId, limit);
  }

  public void start() {
    scheduler.scheduleAtFixedRate(this::updateSlowMode, 0, 10, TimeUnit.SECONDS);
  }
}
