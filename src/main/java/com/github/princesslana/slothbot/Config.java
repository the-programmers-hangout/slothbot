package com.github.princesslana.slothbot;

import com.github.princesslana.smalld.SmallD;
import com.google.common.base.Preconditions;
import disparse.parser.reflection.Injectable;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Config {

  private static final Dotenv ENV = Dotenv.configure().ignoreIfMissing().load();

  private static final SmallD SMALLD = SmallD.create(getDiscordToken());

  private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(32);

  private static final MessageCounter MESSAGE_COUNTER = new MessageCounter(EXECUTOR);
  private static final Limiter LIMITER =
      new Limiter(SMALLD, MESSAGE_COUNTER, EXECUTOR, Path.of(getDataPath(), "limits.json"));
  private static final Self SELF = new Self(SMALLD);

  @Injectable
  public static SmallD getSmallD() {
    return SMALLD;
  }

  public static String getPrefix() {
    return ENV.get("SB_PREFIX", "sb!");
  }

  public static String getDiscordToken() {
    return Preconditions.checkNotNull(ENV.get("SB_TOKEN"));
  }

  public static String getDataPath() {
    return ENV.get("SB_DATA", "data/");
  }

  @Injectable
  public static MessageCounter getMessageCounter() {
    return MESSAGE_COUNTER;
  }

  @Injectable
  public static Limiter getLimiter() {
    return LIMITER;
  }

  @Injectable
  public static Self getSelf() {
    return SELF;
  }
}
