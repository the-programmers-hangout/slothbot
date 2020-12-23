package com.github.princesslana.slothbot.middleware;

import com.github.princesslana.jsonf.GsonF;
import com.github.princesslana.slothbot.Config;
import com.github.princesslana.slothbot.Discord;
import com.github.princesslana.slothbot.Moderator;
import disparse.discord.smalld.Event;
import disparse.discord.smalld.permissions.Permission;
import disparse.discord.smalld.permissions.PermissionUtils;
import java.util.function.BiFunction;

public class PermissionMiddleware implements BiFunction<Event, String, Boolean> {

  private final Moderator moderator;

  public PermissionMiddleware(Moderator moderator) {
    this.moderator = moderator;
  }

  @Override
  public Boolean apply(Event event, String commandName) {
    if (Config.getPublicCommands().contains(commandName)) {
      return true;
    }
    if (PermissionUtils.computeAllPerms(event).contains(Permission.ADMINISTRATOR)) {
      return true;
    }

    var json = GsonF.from(event.getJson()).get("d");

    var possibleGuildId = Discord.getGuild(json);
    var roles = Discord.getRoles(json);
    return possibleGuildId
        .map(guildId -> roles.stream().anyMatch(role -> moderator.contains(guildId, role)))
        .orElse(false);
  }
}
