package com.github.princesslana.slothbot;

import com.github.princesslana.smalld.SmallD;
import com.google.common.base.Preconditions;
import disparse.parser.reflection.Injectable;
import io.github.cdimascio.dotenv.Dotenv;

public class Config {

  private static final Dotenv ENV = Dotenv.configure().ignoreIfMissing().load();

  private static final SmallD SMALLD = SmallD.create(getDiscordToken());

  private static final MessageCounter MESSAGE_COUNTER = new MessageCounter();
  private static final Limiter LIMITER = new Limiter(SMALLD, MESSAGE_COUNTER);

  public static SmallD getSmallD() {
    return SMALLD;
  }

  public static String getPrefix() {
    return ENV.get("SB_PREFIX", "sb!");
  }

  public static String getDiscordToken() {
    return Preconditions.checkNotNull(ENV.get("SB_TOKEN"));
  }

  @Injectable
  public static MessageCounter getMessageCounter() {
    return MESSAGE_COUNTER;
  }

  @Injectable
  public static Limiter getLimiter() {
    return LIMITER;
  }
}
