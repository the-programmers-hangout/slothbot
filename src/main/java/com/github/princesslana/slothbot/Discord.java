package com.github.princesslana.slothbot;

import com.eclipsesource.json.JsonObject;
import com.github.princesslana.jsonf.JsonF;
import disparse.discord.smalld.DiscordRequest;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Discord {
  private Discord() {}

  public static String getChannelId(DiscordRequest req) {
    return req.getDispatcher().channelFromEvent(req.getEvent());
  }

  public static String getGuildId(DiscordRequest req) {
    return req.getDispatcher().guildFromEvent(req.getEvent());
  }

  public static Optional<String> getGuild(JsonF json) {
    return json.get("guild_id").asString();
  }

  public static Set<String> getRoles(JsonF json) {
    return json.get("member", "roles").flatMap(JsonF::asString).collect(Collectors.toSet());
  }

  public static void ifEvent(JsonObject json, String evt, Consumer<JsonObject> f) {
    var isEvent = json.getInt("op", -1) == 0 && json.getString("t", "").equals(evt);

    if (isEvent) {
      f.accept(json.get("d").asObject());
    }
  }
}
