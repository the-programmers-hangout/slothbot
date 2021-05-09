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
      commandName = "count_bots",
      description = "Change if bot messages should be counted",
      acceptFrom = IncomingScope.CHANNEL)
  @Usages({
    @Usage(usage = "true", description = "Count bot messages"),
    @Usage(usage = "false", description = "Don't count bot messages")
  })
  public DiscordResponse countBots() {
    return Try.run(
        () -> {
          Preconditions.checkArgument(request.getArgs().size() == 1, "The value must be given");
          Preconditions.checkArgument(
              request.getArgs().get(0).matches("^true|false$"), "The value must be true or false");
          boolean value = Boolean.parseBoolean(request.getArgs().get(0));
          counter.setBotCounterConfig(guildId, value);
          return DiscordResponse.of(value ? "Bot messages now are counted" : "Bot messages aren't now counted");
        });
  }
}
