package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.MessageCounter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.reflection.CommandHandler;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class DiagnosticCommand {

  private final DiscordRequest request;
  private final MessageCounter counter;

  public DiagnosticCommand(DiscordRequest request, MessageCounter counter) {
    this.request = request;
    this.counter = counter;
  }

  @CommandHandler(commandName = "buckets")
  public DiscordResponse count() {
    return Try.run(
        () -> {
          Preconditions.checkArgument(
              request.getArgs().size() == 1, "You must include a channel id");

          var channelId = request.getArgs().get(0);

          // The way we retreive and format has a built in assumption that
          // the bucket size is 10 seconds.
          var buckets = Lists.partition(counter.getBuckets(channelId), 6);

          var output = new StringJoiner("\n", "```", "```");

          output.add(" Time ago  0s 10s 20s 30s 40s 50s");

          for (int mins = 0; mins < buckets.size(); mins++) {
            var cstr =
                buckets.get(mins).stream()
                    .map(r -> String.format("%3d", r.getCount()))
                    .collect(Collectors.joining(" "));
            output.add(String.format("%7dm %s", mins, cstr));
          }

          return DiscordResponse.of(output.toString());
        });
  }
}
