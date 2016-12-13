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
public class Ref<T> {
  private T myValue;

  public Ref() {
  }

  public Ref(T value) {
    myValue = value;
  }

  public boolean isNull() {
    return myValue == null;
  }

  public T get() {
    return myValue;
  }

  public void set(T value) {
    myValue = value;
  }

  public boolean setIfNull(T value) {
    if (myValue == null) {
      myValue = value;
      return true;
    }
    return false;
  }

  public static <V> Ref<V> create() {
    return new Ref<V>();
  }

  public static <V> Ref<V> create(V value) {
    return new Ref<V>(value);
  }

  @Override
  public String toString() {
    return String.valueOf(myValue);
  }
}
