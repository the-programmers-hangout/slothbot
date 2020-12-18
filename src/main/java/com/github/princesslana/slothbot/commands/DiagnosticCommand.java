package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.Channel;
import com.github.princesslana.slothbot.MessageCounter;
import com.github.princesslana.smalld.SmallD;
import com.google.common.collect.Lists;
import disparse.discord.AbstractPermission;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import disparse.parser.reflection.Usage;
import disparse.parser.reflection.Usages;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class DiagnosticCommand {

  private final SmallD smalld;
  private final DiscordRequest request;
  private final MessageCounter counter;

  public DiagnosticCommand(SmallD smalld, DiscordRequest request, MessageCounter counter) {
    this.smalld = smalld;
    this.request = request;
    this.counter = counter;
  }

  @ParsedEntity
  private static class Options {
    @Flag(
        shortName = 'c',
        longName = "channel",
        description =
            "The id of the channel to rate limit. "
                + "Defaults to the current channel if not provided.")
    public String channelId;
  }

  @CommandHandler(
      commandName = "buckets",
      perms = {AbstractPermission.ADMINISTRATOR},
      description = "View the current bucket counts for the current or specified channel.")
  @Usages({
    @Usage(usage = "", description = "View the current bucket counts for the current channel"),
    @Usage(
        usage = "-c <channel_id>",
        description = "View the current bucket counts for the channel identified by channel_id")
  })
  public DiscordResponse buckets(Options opts) {
    return Try.run(
        () -> {
          var channel = Channel.fromRequest(smalld, request, opts.channelId);

          // The way we retreive and format has a built in assumption that
          // the bucket size is 10 seconds.
          var buckets = Lists.partition(counter.getBuckets(channel), 6);

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
