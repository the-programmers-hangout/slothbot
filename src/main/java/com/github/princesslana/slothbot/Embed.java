package com.github.princesslana.slothbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class Embed {

  private String title;
  private String description;
  private String thumbnailUrl;
  private List<Field> fields = new ArrayList<>();

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setThumbanil(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public void addField(String name, String value) {
    fields.add(new Field(name, value, false));
  }

  public void addInlineField(String name, String value) {
    fields.add(new Field(name, value, true));
  }

  public JsonObject toGson() {
    var embed = new JsonObject();

    if (title != null) {
      embed.addProperty("title", title);
    }

    if (description != null) {
      embed.addProperty("description", description);
    }

    if (thumbnailUrl != null) {
      var thumbnail = new JsonObject();
      thumbnail.addProperty("url", thumbnailUrl);
      embed.add("thumbnail", thumbnail);
    }

    var fieldsJson = new JsonArray();

    fields.stream().map(Field::toGson).forEach(fieldsJson::add);

    if (fieldsJson.size() > 0) {
      embed.add("fields", fieldsJson);
    }

    return embed;
  }

  private static class Field {
    private final String name;
    private final String value;
    private final boolean inline;

    public Field(String name, String value, boolean inline) {
      this.name = name;
      this.value = value;
      this.inline = inline;
    }

    public JsonObject toGson() {
      var json = new JsonObject();
      json.addProperty("name", name);
      json.addProperty("value", value);
      json.addProperty("inline", inline);
      return json;
    }
  }
}
