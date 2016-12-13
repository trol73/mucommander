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

import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class ColorUtil {
  private static int shift(int colorComponent, double d) {
    final int n = (int) (colorComponent * d);
    return n > 255 ? 255 : n < 0 ? 0 : n;
  }

  public static Color shift(Color c, double d) {
    return new Color(shift(c.getRed(), d), shift(c.getGreen(), d), shift(c.getBlue(), d), c.getAlpha());
  }

  public static Color toAlpha(Color color, int a) {
    Color c = color != null ? color : Color.black;
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
  }

  /**
   * Return Color object from string. The following formats are allowed:
   * <code>#abc123</code>,
   * <code>ABC123</code>,
   * <code>ab5</code>,
   * <code>#FFF</code>.
   *
   * @param str hex string
   * @return Color object
   */
  public static Color fromHex(String str) {
    if (str.startsWith("#")) {
      str = str.substring(1);
    }
    if (str.length() == 3) {
      return new Color(
          17 * Integer.valueOf(String.valueOf(str.charAt(0)), 16),
          17 * Integer.valueOf(String.valueOf(str.charAt(1)), 16),
          17 * Integer.valueOf(String.valueOf(str.charAt(2)), 16));
    } else if (str.length() == 6) {
      return Color.decode("0x" + str);
    } else {
      throw new IllegalArgumentException("Should be String of 3 or 6 chars length.");
    }
  }

  public static Color fromHex(String str, Color defaultValue) {
    try {
      return fromHex(str);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * Checks whether color is dark or not based on perceptional luminosity
   * http://stackoverflow.com/questions/596216/formula-to-determine-brightness-of-rgb-color
   *
   * @param c color to check
   * @return dark or not
   */
  public static boolean isDark(final Color c) {
    // based on perceptional luminosity, see
    return (1 - (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255) >= 0.5;
  }
}
