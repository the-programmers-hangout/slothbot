package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.MessageCounter;
import com.google.common.base.Preconditions;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.dispatch.IncomingScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Usage;
import disparse.parser.reflection.Usages;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class ActiveCommand {

  private final DiscordRequest request;
  private final MessageCounter counter;

  public ActiveCommand(DiscordRequest request, MessageCounter counter) {
    this.request = request;
    this.counter = counter;
  }

  @CommandHandler(
      commandName = "active",
      description = "View the channels that are currently most active",
      acceptFrom = IncomingScope.CHANNEL)
  @Usages(@Usage(usage = "", description = "View the channels that are currently most active"))
  public DiscordResponse active() {
    return Try.run(
        () -> {
          Preconditions.checkArgument(
              request.getArgs().isEmpty(), "This command does not take any arguments");

          var counts = counter.getTotalCounts();

          // Java seems to struggle with inference when inlining this within the stream.
          // So currently stuck giving the full (unfortunately log) generic type.
          var ordering =
              Comparator.<Map.Entry<String, Long>, Long>comparing(Map.Entry::getValue).reversed();

          var output =
              counts.entrySet().stream()
                  .sorted(ordering)
                  .limit(5)
                  .map(e -> String.format("<#%s> (%d)", e.getKey(), e.getValue()))
                  .collect(Collectors.joining("\n"));

          if (counts.isEmpty()) {
            output = "There are currently no active channels";
          }

          return DiscordResponse.of(output);
        });
  }
}
