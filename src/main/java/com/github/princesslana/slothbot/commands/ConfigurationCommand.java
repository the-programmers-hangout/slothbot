package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.Channel;
import com.github.princesslana.slothbot.Config;
import com.github.princesslana.slothbot.Discord;
import com.github.princesslana.slothbot.Limiter;
import com.github.princesslana.slothbot.Moderator;
import com.github.princesslana.slothbot.Rate;
import com.github.princesslana.slothbot.Self;
import com.github.princesslana.smalld.SmallD;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.dispatch.IncomingScope;
import disparse.parser.reflection.CommandHandler;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ConfigurationCommand {

  private final SmallD smalld;
  private final DiscordRequest request;
  private final Self self;
  private final Moderator moderator;
  private final Limiter limits;

  public ConfigurationCommand(
      SmallD smalld, DiscordRequest request, Self self, Moderator moderator, Limiter limits) {
    this.smalld = smalld;
    this.request = request;
    this.self = self;
    this.moderator = moderator;
    this.limits = limits;
  }

  @CommandHandler(
      commandName = "configuration",
      description = "Display slothbot's configuraiton",
      acceptFrom = IncomingScope.CHANNEL)
  public DiscordResponse configuration() {
    return DiscordResponse.of(getConfigurationEmbed());
  }

  private JsonObject getConfigurationEmbed() {
    var guildId = Discord.getGuildId(request);

    var thumbnail = new JsonObject();
    thumbnail.addProperty("url", self.getAvatarUrl());

    var prefix = field("Prefix", Config.getPrefix());
    var moderation =
        field(
            "Moderation Roles",
            moderator.get(guildId).stream()
                .map(r -> String.format("%s (%s)", r.getMention(), r.getRoleId()))
                .collect(Collectors.joining("\n")));

    var ratelimits =
        field(
            "Rate Limits",
            limits.getForGuild(guildId).entrySet().stream()
                .map(e -> formatRateLimit(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n")));

    var embed = new JsonObject();
    embed.addProperty("title", "Configuration");
    embed.add("thumbnail", thumbnail);
    embed.add("fields", fields(prefix, moderation, ratelimits));
    return embed;
  }

  private static JsonObject field(String name, String value) {
    var json = new JsonObject();
    json.addProperty("name", name);
    json.addProperty("value", value);
    json.addProperty("inline", false);
    return json;
  }

  private static JsonArray fields(JsonObject... fields) {
    var json = new JsonArray();
    Arrays.stream(fields).forEach(json::add);
    return json;
  }

  private static String formatRateLimit(Channel c, Rate limit) {
    return String.format("%s (%s): %s", c.getMention(), c.getId(), limit.humanize());
  }
}
