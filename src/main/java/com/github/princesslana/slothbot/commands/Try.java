package com.github.princesslana.slothbot.commands;

import disparse.discord.smalld.DiscordResponse;
import java.util.concurrent.Callable;

public class Try {

  private Try() {}

  private static DiscordResponse error(String msg) {
    return DiscordResponse.of("%s %s", Emoji.ERROR, msg);
  }

  public static DiscordResponse run(Callable<DiscordResponse> f) {
    try {
      return f.call();
    } catch (IllegalArgumentException e) {
      return error(e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
