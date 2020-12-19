package com.github.princesslana.slothbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class Message {
  private final JsonObject payload;

  public Message(JsonObject payload) {
    this.payload = payload;
  }

  public Optional<String> getGuild() {
    return Optional.ofNullable(payload.get("guild_id")).map(JsonElement::getAsString);
  }

  public Set<String> getRoles() {
    return Optional.ofNullable(payload.get("member"))
            .map(JsonElement::getAsJsonObject)
            .map(inner -> inner.get("roles"))
            .map(JsonElement::getAsJsonArray)
            .map(JsonArray::iterator)
            .map(this::iteratorToSet)
            .orElseGet(HashSet::new);
  }

  private Set<String> iteratorToSet(Iterator<JsonElement> iterator) {
    Set<String> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next().getAsString());
    }
    return set;
  }
}
