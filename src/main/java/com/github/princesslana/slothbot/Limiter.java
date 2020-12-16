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

  private final ConcurrentMap<Channel, Rate> limits = new ConcurrentHashMap<>();
  private final ConcurrentMap<Channel, Integer> slowmodes = new ConcurrentHashMap<>();

  public Limiter(SmallD smalld, MessageCounter counter, ScheduledExecutorService executor) {
    this.smalld = smalld;
    this.counter = counter;
    this.executor = executor;
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
                    (b, idx) -> b.exceeds(limit) ? (30.0 - idx) / 10.0 : 0.0)
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
  }

  public void set(Channel channel, Rate limit) {
    limits.put(channel, limit);
  }

  public void start() {
    executor.scheduleAtFixedRate(this::updateSlowMode, 10, 10, TimeUnit.SECONDS);
  }
}
