package com.github.princesslana.slothbot;

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

  public static void ifEvent(JsonF json, String evt, Consumer<JsonF> f) {
    if (json.get("op").isEqualTo(0) && json.get("t").isEqualTo(evt)) {
      f.accept(json.get("d"));
    }
  }
}
