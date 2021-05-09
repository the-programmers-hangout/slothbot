package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.Discord;
import com.github.princesslana.slothbot.MessageCounter;
import com.github.princesslana.smalld.SmallD;
import com.google.common.base.Preconditions;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.dispatch.IncomingScope;
import disparse.parser.reflection.*;

public class CounterConfigCommand {

  private final SmallD smalld;
  private final DiscordRequest request;
  private final MessageCounter counter;
  private final String guildId;

  public CounterConfigCommand(SmallD smalld, DiscordRequest request, MessageCounter counter) {
    this.smalld = smalld;
    this.request = request;
    this.counter = counter;
    this.guildId = Discord.getGuildId(request);
  }

  @CommandHandler(
      commandName = "excludebotmsgs",
      aliases = {"ebmsg"},
      description = "Exclude bot messages for slowmode calculation in current server",
      acceptFrom = IncomingScope.CHANNEL)
  @Usages({
    @Usage(
        usage = "",
        description = "Exclude bot messages for slowmode calculation in current server")
  })
  public DiscordResponse excludeBotMsgs() {
    return Try.run(
        () -> {
          Preconditions.checkArgument(
              request.getArgs().isEmpty(), "This command does not take any arguments");
          counter.setBotCounterConfig(guildId, false);
          return DiscordResponse.of("Bot messages are excluded from slowmode calculation now.");
        });
  }

  @CommandHandler(
      commandName = "includebotmsgs",
      aliases = {"ibmsg"},
      description = "Include bot messages for slowmode calculation in current server",
      acceptFrom = IncomingScope.CHANNEL)
  @Usages({
    @Usage(
        usage = "",
        description = "Include bot messages for slowmode calculation in current server")
  })
  public DiscordResponse includeBotMsgs() {
    return Try.run(
        () -> {
          Preconditions.checkArgument(
              request.getArgs().isEmpty(), "This command does not take any arguments");
          counter.setBotCounterConfig(guildId, true);
          return DiscordResponse.of("Bot messages are included in slowmode calculation now.");
        });
  }
}
