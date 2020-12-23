package com.github.princesslana.slothbot;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Optionals {
  private Optionals() {}

  public static <T, U> void ifPresent(Optional<T> t, Optional<U> u, BiConsumer<T, U> f) {
    t.ifPresent(_t -> u.ifPresent(_u -> f.accept(_t, _u)));
  }

  public static <T, U, R> Optional<R> map(Optional<T> t, Optional<U> u, BiFunction<T, U, R> f) {
    return t.flatMap(_t -> u.map(_u -> f.apply(_t, _u)));
  }
}
