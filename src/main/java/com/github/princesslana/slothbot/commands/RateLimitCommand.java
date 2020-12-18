package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.Channel;
import com.github.princesslana.slothbot.Limiter;
import com.github.princesslana.slothbot.Rate;
import com.github.princesslana.smalld.SmallD;
import com.google.common.base.Preconditions;
import disparse.discord.AbstractPermission;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import disparse.parser.reflection.Usage;
import disparse.parser.reflection.Usages;

public class RateLimitCommand {

  private final SmallD smalld;
  private final DiscordRequest request;
  private final Limiter limiter;

  public RateLimitCommand(SmallD smalld, DiscordRequest request, Limiter limiter) {
    this.smalld = smalld;
    this.request = request;
    this.limiter = limiter;
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
      commandName = "ratelimit",
      perms = {AbstractPermission.ADMINISTRATOR},
      description = "Apply a rate limit to the current or specified channel.")
  @Usages({
    @Usage(
        usage = "120",
        description = "Set a rate limit of 120 messages per minute on the current channel"),
    @Usage(
        usage = "-c <channel_id> 60",
        description =
            "Set a rate limit of 60 messages per minute on the channel identified by channel_id"),
    @Usage(
        usage = "-c <channel_id> 0",
        description = "Clear the rate limit on the channel identified by channel_id")
  })
  public DiscordResponse count(Options opts) {
    return Try.run(
        () -> {
          Preconditions.checkArgument(
              request.getArgs().size() == 1, "You must include a rate limit");

          var rateLimit = Integer.parseInt(request.getArgs().get(0));
          Preconditions.checkArgument(rateLimit >= 0, "Rate limit must be >= 0");

          var channel = Channel.fromRequest(smalld, request, opts.channelId);

          if (rateLimit == 0) {
            limiter.clear(channel);
            return DiscordResponse.of(
                String.format(
                    "%s Rate limit for %s cleared", Emoji.CHECKMARK, channel.getMention()));
          }

          var rate = Rate.perMinute(rateLimit);

          limiter.set(channel, rate);

          return DiscordResponse.of(
              String.format(
                  "%s Rate limit of %s set for %s",
                  Emoji.CHECKMARK, rate.humanize(), channel.getMention()));
        });
  }
}
