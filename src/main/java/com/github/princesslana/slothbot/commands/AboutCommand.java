package com.github.princesslana.slothbot.commands;

import com.eclipsesource.json.Json;
import com.github.princesslana.slothbot.Config;
import com.github.princesslana.slothbot.Discord;
import com.github.princesslana.slothbot.Embed;
import com.github.princesslana.slothbot.Self;
import com.github.princesslana.smalld.SmallD;
import com.google.gson.JsonObject;
import disparse.discord.smalld.DiscordResponse;
import disparse.parser.reflection.CommandHandler;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class AboutCommand {

  private final SmallD smalld;
  private final Self self;

  public AboutCommand(SmallD smalld, Self self) {
    this.smalld = smalld;
    this.self = self;
  }

  public static void attachMentionListener(SmallD smalld, Self self) {
    smalld.onGatewayPayload(
        payload -> {
          var json = Json.parse(payload).asObject();

          var cmd = new AboutCommand(smalld, self);
          Discord.ifEvent(json, "MESSAGE_CREATE", cmd::onMessageCreate);
        });
  }

  private void onMessageCreate(com.eclipsesource.json.JsonObject d) {
    if (d.getString("content", "").strip().equals(self.getMention())) {
      var channelId = d.getString("channel_id", "");

      var message = new JsonObject();
      message.add("embed", getAboutEmbed());

      smalld.post(String.format("/channels/%s/messages", channelId), message.toString());
    }
  }

  @CommandHandler(commandName = "about", description = "Display information about slothbot")
  public DiscordResponse about() {
    return DiscordResponse.of(getAboutEmbed());
  }

  private JsonObject getAboutEmbed() {
    var embed = new Embed();
    embed.setTitle("slothbot");
    embed.setDescription("A Discord bot to auto manage slow mode on channels");
    embed.setThumbanil(self.getAvatarUrl());

    embed.addInlineField("Prefix", Config.getPrefix());
    embed.addInlineField("Source", githubLink("the-programmers-hangout", "slothbot", "GitHub"));
    embed.addInlineField(
        "Uptime", DurationFormatUtils.formatDurationWords(self.getUptime().toMillis(), true, true));

    embed.addInlineField(
        "Stack",
        Stream.of(githubLink("princesslana", "smalld"), githubLink("BoscoJared", "disparse"))
            .collect(Collectors.joining("\n")));

    return embed.toGson();
  }

  private static String githubLink(String org, String repo) {
    return githubLink(org, repo, repo);
  }

  private static String githubLink(String org, String repo, String text) {
    return String.format("[%s](https://github.com/%s/%s)", text, org, repo);
  }
}
