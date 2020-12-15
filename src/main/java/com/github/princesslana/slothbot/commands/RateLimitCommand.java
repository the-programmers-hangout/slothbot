package com.github.princesslana.slothbot.commands;

import com.github.princesslana.slothbot.Limiter;
import com.github.princesslana.slothbot.Rate;
import com.google.common.base.Preconditions;
import disparse.discord.AbstractPermission;
import disparse.discord.smalld.DiscordRequest;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.reflection.CommandHandler;

public class RateLimitCommand {

  private final DiscordRequest request;
  private final Limiter limiter;

  public RateLimitCommand(DiscordRequest request, Limiter limiter) {
    this.request = request;
    this.limiter = limiter;
  }

  @CommandHandler(
      commandName = "ratelimit",
      perms = {AbstractPermission.ADMINISTRATOR})
  public DiscordResponse count() {
    return Try.run(
        () -> {
          Preconditions.checkArgument(
              request.getArgs().size() == 1, "You must include a rate limit");

          var rateLimit = Integer.parseInt(request.getArgs().get(0));
          var channelId = request.getDispatcher().channelFromEvent(request.getEvent());

          Preconditions.checkArgument(rateLimit >= 0, "Rate limit must be >= 0");

          if (rateLimit == 0) {
            limiter.clear(channelId);
            return DiscordResponse.of(
                String.format("%s Rate limit for <#%s> cleared", Emoji.CHECKMARK, channelId));
          }

          var rate = Rate.perMinute(rateLimit);

          limiter.set(channelId, rate);

          return DiscordResponse.of(
              String.format(
                  "%s Rate limit of %s set for <#%s>",
                  Emoji.CHECKMARK, rate.humanize(), channelId));
        });
  }
}
