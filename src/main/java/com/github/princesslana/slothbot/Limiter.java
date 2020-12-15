package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.github.princesslana.smalld.SmallD;
import com.google.common.collect.Streams;
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

  private final ConcurrentMap<String, Rate> limits = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Integer> slowmodes = new ConcurrentHashMap<>();

  public Limiter(SmallD smalld, MessageCounter counter, ScheduledExecutorService executor) {
    this.smalld = smalld;
    this.counter = counter;
    this.executor = executor;
  }

  private void updateSlowMode() {
    LOG.debug("Updating slow mode for {} channels...", limits.size());
    limits.keySet().forEach(cid -> CompletableFuture.runAsync(() -> updateSlowMode(cid), executor));
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

    LOG.debug("Calculated slow mode for channel {} as {} seconds.", channelId, slowmode);
    updateSlowMode(channelId, slowmode);
  }

  private void updateSlowMode(String channelId, int seconds) {
    if (slowmodes.containsKey(channelId) && slowmodes.get(channelId).intValue() == seconds) {
      LOG.debug("Slowmode for channel {} already {} seconds.", channelId, seconds);
      return;
    }

    LOG.debug("Updating slowmode for for channel {} to {} seconds...", channelId, seconds);
    smalld.patch(
        "/channels/" + channelId, Json.object().add("rate_limit_per_user", seconds).toString());

    slowmodes.put(channelId, seconds);
  }

  public void clear(String channelId) {
    limits.remove(channelId);
    updateSlowMode(channelId, 0);
  }

  public void set(String channelId, Rate limit) {
    limits.put(channelId, limit);
  }

  public void start() {
    executor.scheduleAtFixedRate(this::updateSlowMode, 0, 10, TimeUnit.SECONDS);
  }
}
