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

import java.util.Collection;

/**
 * @author Konstantin Bulenkov
 */
@SuppressWarnings({"UtilityClassWithoutPrivateConstructor", "SSBasedInspection"})
public class ArrayUtilRt {
  private static final int ARRAY_COPY_THRESHOLD = 20;

  public static final String[] EMPTY_STRING_ARRAY = new String[0];


  public static String[] toStringArray(Collection<String> collection) {
    return collection == null || collection.isEmpty()
        ? EMPTY_STRING_ARRAY : toArray(collection, new String[collection.size()]);
  }

  /**
   * This is a replacement for {@link Collection#toArray(Object[])}. For small collections it is faster to stay at java level and refrain
   * from calling JNI {@link System#arraycopy(Object, int, Object, int, int)}
   */

  public static <T> T[] toArray(Collection<T> c, T[] sample) {
    final int size = c.size();
    if (size == sample.length && size < ARRAY_COPY_THRESHOLD) {
      int i = 0;
      for (T t : c) {
        sample[i++] = t;
      }
      return sample;
    }

    return c.toArray(sample);
  }

  /**
   * @param src source array.
   * @param obj object to be found.
   * @return index of <code>obj</code> in the <code>src</code> array.
   * Returns <code>-1</code> if passed object isn't found. This method uses
   * <code>equals</code> of arrays elements to compare <code>obj</code> with
   * these elements.
   */
  public static <T> int find(final T[] src, final T obj) {
    for (int i = 0; i < src.length; i++) {
      final T o = src[i];
      if (o == null) {
        if (obj == null) {
          return i;
        }
      } else {
        if (o.equals(obj)) {
          return i;
        }
      }
    }
    return -1;
  }
}
