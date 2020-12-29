package com.github.princesslana.slothbot;

import com.github.princesslana.jsonf.JsonF;
import com.github.princesslana.smalld.SmallD;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import disparse.discord.smalld.DiscordRequest;
import java.util.Objects;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;

public class Role {
  private final String guildId;
  private final String roleId;

  public Role(String guildId, String roleId) {
    this.guildId = Preconditions.checkNotNull(guildId);
    this.roleId = Preconditions.checkNotNull(roleId);
  }

  public String getGuildId() {
    return guildId;
  }

  public String getRoleId() {
    return roleId;
  }

  public String getMention() {
    return String.format("<@&%s>", roleId);
  }

  public boolean exists(SmallD smalld) {
    var json = JsonF.parse(smalld.get(String.format("/guilds/%s/roles", guildId)));

    return json.flatMap(j -> j.get("id").asString()).anyMatch(Predicate.isEqual(roleId));
  }

  @Override
  public int hashCode() {
    return Objects.hash(guildId, roleId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != this.getClass()) {
      return false;
    }
    Role rhs = (Role) obj;

    return Objects.equals(guildId, rhs.guildId) && Objects.equals(roleId, rhs.roleId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("guildId", guildId)
        .add("roleId", roleId)
        .toString();
  }

  public static Role fromRequest(SmallD smalld, DiscordRequest req, String userInput) {
    var guildId = Discord.getGuildId(req);

    if (StringUtils.isNumeric(StringUtils.strip(userInput, " <@&>"))) {
      var roleId = StringUtils.strip(userInput, "<@&>");

      var role = new Role(guildId, roleId);

      Preconditions.checkArgument(
          role.exists(smalld), String.format("Role %s is not a role in this guild", roleId));

      return role;
    } else {
      throw new IllegalArgumentException(
          String.format("Role %s is not a role in this guild", userInput));
    }
  }
}
