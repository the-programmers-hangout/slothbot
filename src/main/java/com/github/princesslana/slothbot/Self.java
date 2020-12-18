package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.princesslana.smalld.SmallD;
import java.time.Duration;
import java.time.Instant;

public class Self {

  private static final String DISCORD_CDN = "https://cdn.discordapp.com";

  private static Instant up = Instant.now();

  private JsonObject user;

  public Self(SmallD smalld) {
    smalld.onGatewayPayload(this::onGatewayPayload);
  }

  public Duration getUptime() {
    return Duration.between(up, Instant.now());
  }

  private String getDiscriminator() {
    return user.getString("discriminator", "");
  }

  private String getUserId() {
    return user.getString("id", "");
  }

  public String getMention() {
    return String.format("<@!%s>", getUserId());
  }

  public String getAvatarUrl() {
    var hash = user.get("avatar");

    return hash.isNull()
        ? String.format(
            "%s/embed/avatars/%d.png", DISCORD_CDN, Integer.parseInt(getDiscriminator()) % 5)
        : String.format("%s/avatars/%s/%s.png", DISCORD_CDN, getUserId(), hash.asString());
  }

  private void onGatewayPayload(String payload) {
    var json = Json.parse(payload).asObject();

    Discord.ifEvent(json, "READY", this::onReady);
  }

  private void onReady(JsonObject d) {
    user = d.get("user").asObject();
  }
}
