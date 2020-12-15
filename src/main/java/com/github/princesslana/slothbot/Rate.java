package com.github.princesslana.slothbot;

import java.time.Duration;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class Rate {
  private final long count;
  private final Duration duration;

  private Rate(long count, Duration duration) {
    this.count = count;
    this.duration = duration;
  }

  public long getCount() {
    return count;
  }

  private double getCountPerSecond() {
    return (double) count / duration.toSeconds();
  }

  public boolean exceeds(Rate r) {
    return getCountPerSecond() > r.getCountPerSecond();
  }

  public String humanize() {
    return String.format(
        "%d per %s",
        count, DurationFormatUtils.formatDurationWords(duration.toMillis(), true, true));
  }

  public static Rate per(Duration duration, long count) {
    return new Rate(count, duration);
  }

  public static Rate perMinute(long count) {
    return per(Duration.ofMinutes(1), count);
  }
}
