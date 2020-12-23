package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.princesslana.jsonf.JsonF;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Optional;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class Rate {
  private final long count;
  private final Duration duration;

  private Rate(long count, Duration duration) {
    Preconditions.checkArgument(count >= 0, "count must be greater than zero");
    Preconditions.checkArgument(duration.toSeconds() >= 0, "duration must be greater than zero");

    this.count = count;
    this.duration = duration;
  }

  public long getCount() {
    return count;
  }

  public double getCountPerSecond() {
    return (double) count / duration.toSeconds();
  }

  public boolean exceeds(Rate r) {
    return getCountPerSecond() > r.getCountPerSecond();
  }

  public double ratioTo(Rate r) {
    return getCountPerSecond() / r.getCountPerSecond();
  }

  public String humanize() {
    return String.format(
        "%d per %s",
        count, DurationFormatUtils.formatDurationWords(duration.toMillis(), true, true));
  }

  public JsonObject toJson() {
    return Json.object().add("count", count).add("seconds", duration.toSeconds());
  }

  public static Optional<Rate> fromJson(JsonF json) {
    var count = json.get("count").asNumber().map(Number::longValue);
    var duration = json.get("seconds").asNumber().map(Number::longValue).map(Duration::ofSeconds);

    return Optionals.map(count, duration, Rate::new);
  }

  public static Rate per(Duration duration, long count) {
    return new Rate(count, duration);
  }

  public static Rate perMinute(long count) {
    return per(Duration.ofMinutes(1), count);
  }
}
