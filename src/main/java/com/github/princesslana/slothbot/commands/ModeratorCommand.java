package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.Discord;
import com.github.princesslana.slothbot.Moderator;
import com.github.princesslana.slothbot.Role;
import com.github.princesslana.smalld.SmallD;
import com.google.common.base.Preconditions;
import disparse.discord.AbstractPermission;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.dispatch.IncomingScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Usage;
import disparse.parser.reflection.Usages;
import java.util.stream.Collectors;

public class ModeratorCommand {

  private final SmallD smalld;
  private final DiscordRequest request;
  private final Moderator moderator;

  public ModeratorCommand(SmallD smalld, DiscordRequest request, Moderator moderator) {
    this.smalld = smalld;
    this.request = request;
    this.moderator = moderator;
  }

  @CommandHandler(
      commandName = "moderator.add",
      description = "Admin only command to add which roles are considered to be moderators.",
      acceptFrom = IncomingScope.CHANNEL,
      perms = {AbstractPermission.ADMINISTRATOR})
  @Usages({@Usage(usage = "718166777405767771", description = "add this roleId as a moderator")})
  public DiscordResponse addModerator() {
    return Try.run(
        () -> {
          Preconditions.checkArgument(request.getArgs().size() == 1, "A single role must be given");

          var role = Role.fromRequest(smalld, request, request.getArgs().get(0));

          moderator.add(role);

          return DiscordResponse.of(
              "%s Successfully added %s as moderator role", Emoji.CHECKMARK, role.getMention());
        });
  }

  @CommandHandler(
      commandName = "moderator.remove",
      description = "Admin only command to remove which roles are considered to be moderators.",
      acceptFrom = IncomingScope.CHANNEL,
      perms = {AbstractPermission.ADMINISTRATOR})
  @Usages({@Usage(usage = "718166777405767771", description = "remove this roleId as a moderator")})
  public DiscordResponse removeModerator() {
    return Try.run(
        () -> {
          Preconditions.checkArgument(request.getArgs().size() == 1, "A single role must be given");
          var guildId = Discord.getGuildId(request);
          var userInput = request.getArgs().get(0);

          var role = new Role(guildId, userInput);
          if (!moderator.contains(role)) {
            role = Role.fromRequest(smalld, request, userInput);
          }

          moderator.remove(role);

          return DiscordResponse.of(
              "Successfully removed %s from moderator roles", role.getMention());
        });
  }

  @CommandHandler(
      commandName = "moderator.list",
      description = "Admin only command to list which roles are considered to be moderators.",
      acceptFrom = IncomingScope.CHANNEL,
      perms = {AbstractPermission.ADMINISTRATOR})
  @Usages({
    @Usage(usage = "", description = "list all the roles which are considered to be moderators.")
  })
  public DiscordResponse listModerator() {
    var guildId = Discord.getGuildId(request);

    var roles =
        moderator.get(guildId).stream()
            .map(r -> String.format("%s (%s)", r.getMention(), r.getRoleId()))
            .collect(Collectors.joining("\n"));

    return DiscordResponse.of("Moderator roles: \n%s", roles);
  }
}
