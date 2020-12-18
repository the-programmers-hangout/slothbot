package com.github.princesslana.slothbot;

import com.eclipsesource.json.JsonObject;
import disparse.discord.smalld.DiscordRequest;
import java.util.function.Consumer;

public class Discord {
  private Discord() {}

  public static String getChannelId(DiscordRequest req) {
    return req.getDispatcher().channelFromEvent(req.getEvent());
  }

  public static String getGuildId(DiscordRequest req) {
    return req.getDispatcher().guildFromEvent(req.getEvent());
  }

  public static void ifEvent(JsonObject json, String evt, Consumer<JsonObject> f) {
    var isEvent = json.getInt("op", -1) == 0 && json.getString("t", "").equals(evt);

    if (isEvent) {
      f.accept(json.get("d").asObject());
    }
  }
}
