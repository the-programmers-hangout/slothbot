package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.princesslana.jsonf.JsonF;
import com.github.princesslana.jsonf.MinimalF;
import com.github.princesslana.smalld.HttpException;
import com.github.princesslana.smalld.SmallD;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import disparse.discord.smalld.DiscordRequest;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class Channel {
  private final String guildId;
  private final String channelId;

  public Channel(String guildId, String channelId) {
    this.guildId = Preconditions.checkNotNull(guildId);
    this.channelId = Preconditions.checkNotNull(channelId);
  }

  public String getId() {
    return channelId;
  }

  public String getGuildId() {
    return guildId;
  }

  public String getMention() {
    return String.format("<#%s>", channelId);
  }

  public JsonObject toJson() {
    return Json.object().add("channelId", channelId).add("guildId", guildId);
  }

  public boolean exists(SmallD smalld) {
    try {
      var json = MinimalF.parse(smalld.get(String.format("/channels/%s", channelId)));
      return json.get("guild_id").asString().map(g -> g.equals(guildId)).orElse(false);
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

  public static Optional<Channel> fromJson(JsonF json) {
    var guildId = json.get("guildId").asString();
    var channelId = json.get("channelId").asString();

    return Optionals.map(guildId, channelId, Channel::new);
  }

  public static Channel fromRequest(SmallD smalld, DiscordRequest req, String userInput) {
    var guildId = Discord.getGuildId(req);
    var channelId = Discord.getChannelId(req);

    if (StringUtils.isNumeric(StringUtils.strip(userInput, " <#>"))) {
      channelId = StringUtils.strip(userInput, " <#>");
    } else if (userInput != null) {
      throw new IllegalArgumentException(
          String.format("Channel %s is not a channel in this guild", userInput));
    }

    var channel = new Channel(guildId, channelId);

    Preconditions.checkArgument(
        channel.exists(smalld),
        String.format("Channel %s is not a channel in this guild", channelId));

    return channel;
  }
}
