/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bulenkov.iconloader.util;

/**
 * @author Konstantin Bulenkov
 */
public class Pair<FIRST, SECOND> {
  public final FIRST first;
  public final SECOND second;

  @SuppressWarnings("unchecked")
  private static final Pair EMPTY = create(null, null);

  @SuppressWarnings("unchecked")
  public static <F, S> Pair<F, S> empty() {
    return EMPTY;
  }

  public Pair(FIRST first, SECOND second) {
    this.first = first;
    this.second = second;
  }

  public final FIRST getFirst() {
    return first;
  }

  public final SECOND getSecond() {
    return second;
  }

  public final boolean equals(Object o) {
    return o instanceof Pair
        && ComparingUtils.equal(first, ((Pair) o).first)
        && ComparingUtils.equal(second, ((Pair) o).second);
  }

  public int hashCode() {
    int result = first != null ? first.hashCode() : 0;
    result = 31 * result + (second != null ? second.hashCode() : 0);
    return result;
  }

  public String toString() {
    return "<" + first + ", " + second + ">";
  }

  public static <F, S> Pair<F, S> create(F first, S second) {
    return new Pair<F, S>(first, second);
  }

  public static <T> T getFirst(Pair<T, ?> pair) {
    return pair != null ? pair.first : null;
  }

  public static <T> T getSecond(Pair<?, T> pair) {
    return pair != null ? pair.second : null;
  }
}
