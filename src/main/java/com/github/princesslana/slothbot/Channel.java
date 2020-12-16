package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.github.princesslana.smalld.HttpException;
import com.github.princesslana.smalld.SmallD;
import com.google.common.base.MoreObjects;
import java.util.Objects;

public class Channel {
  private final String guildId;
  private final String channelId;

  public Channel(String guildId, String channelId) {
    this.guildId = guildId;
    this.channelId = channelId;
  }

  public String getId() {
    return channelId;
  }

  public boolean exists(SmallD smalld) {
    try {
      var json = Json.parse(smalld.get(String.format("/channels/%s", channelId))).asObject();
      return json.getString("guild_id", "").equals(guildId);
    } catch (HttpException e) {
      if (e.getCode() == 404) {
        return false;
      }
      throw e;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(guildId, channelId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != this.getClass()) {
      return false;
    }
    Channel rhs = (Channel) obj;

    return Objects.equals(guildId, rhs.guildId) && Objects.equals(channelId, rhs.channelId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("guildId", guildId)
        .add("channelId", channelId)
        .toString();
  }
}
