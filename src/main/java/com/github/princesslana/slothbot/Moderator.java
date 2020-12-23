package com.github.princesslana.slothbot;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.WriterConfig;
import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Moderator {
  private static final Logger LOG = LogManager.getLogger(Moderator.class);

  private final HashMultimap<String, String> guildToModeratorRoles = HashMultimap.create();
  private final Path savePath;

  public Moderator(Path savePath) {
    this.savePath = savePath;
  }

  public void add(Role role) {
    guildToModeratorRoles.put(role.getGuildId(), role.getRoleId());
    save();
  }

  public void remove(Role role) {
    guildToModeratorRoles.remove(role.getGuildId(), role.getRoleId());
    save();
  }

  public boolean contains(Role role) {
    return guildToModeratorRoles.containsEntry(role.getGuildId(), role.getRoleId());
  }

  public Set<Role> get(String guildId) {
    return guildToModeratorRoles.get(guildId).stream()
        .map(rid -> new Role(guildId, rid))
        .collect(Collectors.toSet());
  }

  private void save() {
    var json = Json.array();

    guildToModeratorRoles
        .asMap()
        .forEach(
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
          guildToModeratorRoles.put(guildId, role.asString());
        }
      }

      LOG.atDebug().log("Loaded {} guilds' mappings from {}", arr.size(), savePath);
    } catch (NoSuchFileException e) {
      LOG.atInfo().log("No limits file at {}. One will be created if needed.", savePath);
    } catch (IOException e) {
      LOG.atWarn().withThrowable(e).log("Error loading guild mappings from {}", savePath);
    }
  }
}
