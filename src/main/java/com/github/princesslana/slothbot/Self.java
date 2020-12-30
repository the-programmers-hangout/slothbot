package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.github.princesslana.jsonf.JsonF;
import com.github.princesslana.smalld.SmallD;
import java.time.Duration;
import java.time.Instant;

public class Self {

  private static final String DISCORD_CDN = "https://cdn.discordapp.com";

  private static Instant up = Instant.now();

  private JsonF user;

  public Self(SmallD smalld) {
    smalld.onGatewayPayload(this::onGatewayPayload);
  }

  public Duration getUptime() {
    return Duration.between(up, Instant.now());
  }

  private String getDiscriminator() {
    return user.get("discriminator").asString().orElseThrow(IllegalStateException::new);
  }

  private String getUserId() {
    return user.get("id").asString().orElseThrow(IllegalStateException::new);
  }

  public boolean isMention(String s) {
    return s != null && s.replaceAll("[<>!@]", "").equals(getUserId());
  }

  public String getAvatarUrl() {
    return user.get("avatar")
        .asString()
        .map(hsh -> String.format("%s/avatars/%s/%s.png", DISCORD_CDN, getUserId(), hsh))
        .orElse(
            String.format(
                "%s/embed/avatars/%d.png", DISCORD_CDN, Integer.parseInt(getDiscriminator()) % 5));
  }

  private void onGatewayPayload(String payload) {
    var json = Json.parse(payload).asObject();

    Discord.ifEvent(JsonF.parse(payload), "READY", this::onReady);
  }

  private void onReady(JsonF d) {
    user = d.get("user");
  }
}
