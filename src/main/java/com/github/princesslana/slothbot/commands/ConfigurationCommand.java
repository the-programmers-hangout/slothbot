package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.*;
import com.github.princesslana.smalld.SmallD;
import com.google.gson.JsonObject;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.dispatch.IncomingScope;
import disparse.parser.reflection.CommandHandler;
import java.util.stream.Collectors;

public class ConfigurationCommand {

  private final SmallD smalld;
  private final DiscordRequest request;
  private final Self self;
  private final Moderator moderator;
  private final Limiter limits;
  private final MessageCounter counter;

  public ConfigurationCommand(
      SmallD smalld,
      DiscordRequest request,
      Self self,
      Moderator moderator,
      Limiter limits,
      MessageCounter counter) {
    this.smalld = smalld;
    this.request = request;
    this.self = self;
    this.moderator = moderator;
    this.limits = limits;
    this.counter = counter;
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

    var embed = new Embed();

    embed.setTitle("Configuration");
    embed.setThumbanil(self.getAvatarUrl());

    embed.addField("Prefix", Config.getPrefix());

    var moderationRoles =
        moderator.get(guildId).stream()
            .map(r -> String.format("%s (%s)", r.getMention(), r.getRoleId()))
            .collect(Collectors.joining("\n"));

    embed.addField("Moderation Roles", moderationRoles.isBlank() ? "None" : moderationRoles);

    var rateLimits =
        limits.getForGuild(guildId).entrySet().stream()
            .map(e -> formatRateLimit(e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n"));

    embed.addField("Rate Limits", rateLimits.isBlank() ? "None" : rateLimits);

    embed.addField("Count bot messages", counter.getBotCounterConfig(guildId) ? "Yes" : "No");

    return embed.toGson();
  }

  private static String formatRateLimit(Channel c, Rate limit) {
    return String.format("%s (%s): %s", c.getMention(), c.getId(), limit.humanize());
  }
}
