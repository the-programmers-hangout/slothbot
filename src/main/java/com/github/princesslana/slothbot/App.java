package com.github.princesslana.slothbot;

import com.github.princesslana.slothbot.commands.AboutCommand;
import disparse.discord.smalld.Dispatcher;

public class App {
  public static void main(String[] args) {
    var disparse =
        new Dispatcher.Builder(App.class)
            .withSmalldClient(Config.getSmallD())
            .prefix(Config.getPrefix())
            .build();

    Dispatcher.init(disparse);

    Config.getMessageCounter().start(Config.getSmallD());

    Config.getLimiter().load();
    Config.getLimiter().start();

    AboutCommand.attachMentionListener(Config.getSmallD(), Config.getSelf());

    Config.getSmallD().run();
  }
}
