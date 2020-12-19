package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.WriterConfig;
import com.google.common.base.Charsets;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Moderator {
  private static final Logger LOG = LogManager.getLogger(Limiter.class);

  private final Map<String, Set<String>> guildToModeratorRoles = new HashMap<>();
  private final Path savePath;

  public Moderator(Path savePath) {
    this.savePath = savePath;
  }

  public void addModeratorRoleForGuild(String guildId, String roleId) {
    ensureSetForKey(guildId).add(roleId);
    save();
  }

  public boolean removeModeratorRoleForGuild(String guildId, String roleId) {
    var removed = ensureSetForKey(guildId).remove(roleId);
    save();
    return removed;
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

  private void save() {
    var json = Json.array();

    guildToModeratorRoles.forEach(
        (k, v) -> {
          var obj = Json.object().add("guild", k);
          var arr = new JsonArray();
          v.forEach(arr::add);
          json.add(obj.add("roles", arr));
        });

    try {
      MoreFiles.asCharSink(savePath, Charsets.UTF_8)
          .write(json.toString(WriterConfig.PRETTY_PRINT));
      LOG.atDebug().log("Saved {} guilds' mappings to {}", json.size(), savePath);
    } catch (IOException e) {
      LOG.atWarn().withThrowable(e).log("Error saving guild mappings to {}", savePath);
    }
  }

  public void load() {
    try {
      var arr = Json.parse(MoreFiles.asCharSource(savePath, Charsets.UTF_8).read()).asArray();

      for (var json : arr) {
        var obj = json.asObject();
        var guildId = obj.get("guild").asString();
        var rolesArray = obj.get("roles").asArray();
        for (var role : rolesArray) {
          ensureSetForKey(guildId).add(role.asString());
        }
      }

      LOG.atDebug().log("Loaded {} guilds' mappings from {}", arr.size(), savePath);
    } catch (IOException e) {
      LOG.atWarn().withThrowable(e).log("Error loading guild mappings from {}", savePath);
    }
  }
}
