package com.github.princesslana.slothbot.middleware;

import com.github.princesslana.slothbot.Config;
import com.github.princesslana.slothbot.Message;
import disparse.discord.smalld.Event;

import java.util.function.BiFunction;

public class PermissionMiddleware implements BiFunction<Event, String, Boolean> {
  @Override
  public Boolean apply(Event event, String commandName) {
    if (!Config.getSensitiveCommands().contains(commandName)) {
      return true;
    }
    Message message = new Message(event.getJson().getAsJsonObject("d"));
    var possibleGuildId = message.getGuild();
    var roles = message.getRoles();
    return possibleGuildId.map(guildId -> roles.stream()
            .anyMatch(role -> Config.getModerator().containsModeratorRoleForGuild(guildId, role))).orElse(true);
  }
}
