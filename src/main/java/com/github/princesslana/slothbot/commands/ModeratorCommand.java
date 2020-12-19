package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.Discord;
import com.github.princesslana.slothbot.Moderator;
import disparse.discord.AbstractPermission;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.dispatch.IncomingScope;
import disparse.parser.reflection.*;


public class ModeratorCommand {

  private final DiscordRequest request;
  private final Moderator moderator;

  public ModeratorCommand(DiscordRequest request, Moderator moderator) {
    this.request = request;
    this.moderator = moderator;
  }

  @CommandHandler(commandName = "moderator.add",
          description = "Admin only command to configure which roles are considered to be moderators.",
          acceptFrom = IncomingScope.CHANNEL,
          perms = {AbstractPermission.ADMINISTRATOR})
  @Usages({
          @Usage(usage = "718166777405767771", description = "add this roleId as a moderator"),
          @Usage(usage = "718166777405767771 424095817939419146", description = "add all listed roleIds as a moderator")
  })
  public DiscordResponse addModerator() {
    var guildId = Discord.getGuildId(request);
    var args = request.getArgs();
    args.forEach(role -> moderator.addModeratorRoleForGuild(guildId, role));
    return DiscordResponse.of("Successfully added %d roles", args.size());
  }
}
