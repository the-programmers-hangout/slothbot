package com.github.princesslana.slothbot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Moderator {
  private final Map<String, Set<String>> guildToModeratorRoles = new HashMap<>();

  public Moderator() {}

  public void addModeratorRoleForGuild(String guildId, String roleId) {
    ensureSetForKey(guildId).add(roleId);
  }

  public boolean removeModeratorRoleForGuild(String guildId, String roleId) {
    return ensureSetForKey(guildId).remove(roleId);
  }

  public boolean containsModeratorRoleForGuild(String guildId, String roleId) {
    return ensureSetForKey(guildId).contains(roleId);
  }

  public Set<String> getModeratorRoleForGuild(String guildId) {
    return ensureSetForKey(guildId);
  }

  private Set<String> ensureSetForKey(String guildId) {
    return guildToModeratorRoles.computeIfAbsent(guildId, _k -> new HashSet<>());
  }

}
