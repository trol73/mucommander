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

import com.bulenkov.iconloader.IsRetina;
import com.bulenkov.iconloader.JBHiDPIScaledImage;
import com.bulenkov.iconloader.RetinaImage;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.reflect.Field;

/**
 * @author Konstantin Bulenkov
 */
public class UIUtil {
  public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
  private static volatile Pair<String, Integer> ourSystemFontData;
  public static final float DEF_SYSTEM_FONT_SIZE = 12f; // TODO: consider 12 * 1.33 to compensate JDK's 72dpi font scale

  public static <T extends JComponent> T findComponentOfType(JComponent parent, Class<T> cls) {
    if (parent == null || cls.isAssignableFrom(parent.getClass())) {
      @SuppressWarnings({"unchecked"}) final T t = (T) parent;
      return t;
    }
    for (Component component : parent.getComponents()) {
      if (component instanceof JComponent) {
        T comp = findComponentOfType((JComponent) component, cls);
        if (comp != null) return comp;
      }
    }
    return null;
  }

  @Nullable
  public static Pair<String, Integer> getSystemFontData() {
    return ourSystemFontData;
  }

  public static <T> T getParentOfType(Class<? extends T> cls, Component c) {
    Component eachParent = c;
    while (eachParent != null) {
      if (cls.isAssignableFrom(eachParent.getClass())) {
        @SuppressWarnings({"unchecked"}) final T t = (T) eachParent;
        return t;
      }

      eachParent = eachParent.getParent();
    }

    return null;
  }

  public static boolean isAppleRetina() {
    return isRetina() && SystemInfo.isAppleJvm;
  }

  public static Color getControlColor() {
    return UIManager.getColor("control");
  }

  public static Color getPanelBackground() {
    return UIManager.getColor("Panel.background");
  }

  public static boolean isUnderDarcula() {
    return UIManager.getLookAndFeel().getName().equals("Darcula");
  }

  public static Color getListBackground() {
    return UIManager.getColor("List.background");
  }

  public static Color getListForeground() {
    return UIManager.getColor("List.foreground");
  }

  public static Color getLabelForeground() {
    return UIManager.getColor("Label.foreground");
  }

  public static Color getTextFieldBackground() {
    return UIManager.getColor("TextField.background");
  }

  public static Color getTreeSelectionForeground() {
    return UIManager.getColor("Tree.selectionForeground");
  }

  public static Color getTreeForeground() {
    return UIManager.getColor("Tree.foreground");
  }

  private static final Color DECORATED_ROW_BG_COLOR = new DoubleColor(new Color(242, 245, 249), new Color(65, 69, 71));

  public static Color getDecoratedRowColor() {
    return DECORATED_ROW_BG_COLOR;
  }

  public static Color getTreeSelectionBackground(boolean focused) {
    return focused ? getTreeSelectionBackground() : getTreeUnfocusedSelectionBackground();
  }

  private static Color getTreeSelectionBackground() {
    return UIManager.getColor("Tree.selectionBackground");
  }

  public static Color getTreeUnfocusedSelectionBackground() {
    Color background = getTreeTextBackground();
    return ColorUtil.isDark(background) ? new DoubleColor(Gray._30, new Color(13, 41, 62)) : Gray._212;
  }

  public static Color getTreeTextBackground() {
    return UIManager.getColor("Tree.textBackground");
  }

  public static void drawImage(Graphics g, Image image, int x, int y, ImageObserver observer) {
    if (image instanceof JBHiDPIScaledImage) {
      final Graphics2D newG = (Graphics2D) g.create(x, y, image.getWidth(observer), image.getHeight(observer));
      newG.scale(0.5, 0.5);
      Image img = ((JBHiDPIScaledImage) image).getDelegate();
      if (img == null) {
        img = image;
      }
      newG.drawImage(img, 0, 0, observer);
      newG.scale(1, 1);
      newG.dispose();
    } else {
      g.drawImage(image, x, y, observer);
    }
  }


  private static final Ref<Boolean> ourRetina = Ref.create(SystemInfo.isMac ? null : false);

  public static boolean isRetina() {
    synchronized (ourRetina) {
      if (ourRetina.isNull()) {
        ourRetina.set(false); // in case HiDPIScaledImage.drawIntoImage is not called for some reason

        if (SystemInfo.isJavaVersionAtLeast("1.6.0_33") && SystemInfo.isAppleJvm) {
          if (!"false".equals(System.getProperty("ide.mac.retina"))) {
            ourRetina.set(IsRetina.isRetina());
            return ourRetina.get();
          }
        } else if (SystemInfo.isJavaVersionAtLeast("1.7.0_40") && SystemInfo.isOracleJvm) {
          GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
          final GraphicsDevice device = env.getDefaultScreenDevice();
          try {
            Field field = device.getClass().getDeclaredField("scale");
            if (field != null) {
              field.setAccessible(true);
              Object scale = field.get(device);
              if (scale instanceof Integer && (Integer) scale == 2) {
                ourRetina.set(true);
                return true;
              }
            }
          } catch (Exception ignore) {
          }
        }
        ourRetina.set(false);
      }

      return ourRetina.get();
    }
  }

  public static BufferedImage createImage(int width, int height, int type) {
    if (isRetina()) {
      return RetinaImage.create(width, height, type);
    }
    //noinspection UndesirableClassUsage
    return new BufferedImage(width, height, type);
  }


  private static final GrayFilter DEFAULT_GRAY_FILTER = new GrayFilter(true, 65);
  private static final GrayFilter DARCULA_GRAY_FILTER = new GrayFilter(true, 30);

  public static GrayFilter getGrayFilter() {
    return isUnderDarcula() ? DARCULA_GRAY_FILTER : DEFAULT_GRAY_FILTER;
  }

  public static Font getLabelFont() {
    return UIManager.getFont("Label.font");
  }

  public static float getFontSize(FontSize size) {
    int defSize = getLabelFont().getSize();
    switch (size) {
      case SMALL:
        return Math.max(defSize - JBUI.scale(2f), JBUI.scale(11f));
      case MINI:
        return Math.max(defSize - JBUI.scale(4f), JBUI.scale(9f));
      default:
        return defSize;
    }
  }

  public enum FontSize {NORMAL, SMALL, MINI}

  public static void initSystemFontData() {
    if (ourSystemFontData != null) return;

    // With JB Linux JDK the label font comes properly scaled based on Xft.dpi settings.
    Font font = getLabelFont();

    Float forcedScale = null;
    if (Registry.is("ide.ui.scale.override")) {
      forcedScale = Registry.getFloat("ide.ui.scale");
    }
    else if (SystemInfo.isLinux && !SystemInfo.isJetbrainsJvm) {
      // With Oracle JDK: derive scale from X server DPI
      float scale = getScreenScale();
      if (scale > 1f) {
        forcedScale = scale;
      }
      // Or otherwise leave the detected font. It's undetermined if it's scaled or not.
      // If it is (likely with GTK DE), then the UI scale will be derived from it,
      // if it's not, then IDEA will start unscaled. This lets the users of GTK DEs
      // not to bother about X server DPI settings. Users of other DEs (like KDE)
      // will have to set X server DPI to meet their display.
    }
    else if (SystemInfo.isWindows) {
      //noinspection HardCodedStringLiteral
      Font winFont = (Font)Toolkit.getDefaultToolkit().getDesktopProperty("win.messagebox.font");
      if (winFont != null) {
        font = winFont; // comes scaled
      }
    }
    if (forcedScale != null) {
      // With forced scale, we derive font from a hard-coded value as we cannot be sure
      // the system font comes unscaled.
      font = font.deriveFont(DEF_SYSTEM_FONT_SIZE * forcedScale.floatValue());
    }
    ourSystemFontData = Pair.create(font.getName(), font.getSize());
  }

  private static float getScreenScale() {
    int dpi = 96;
    try {
      dpi = Toolkit.getDefaultToolkit().getScreenResolution();
    } catch (HeadlessException e) {
    }
    float scale = 1f;
    if (dpi < 120) scale = 1f;
    else if (dpi < 144) scale = 1.25f;
    else if (dpi < 168) scale = 1.5f;
    else if (dpi < 192) scale = 1.75f;
    else scale = 2f;

    return scale;
  }
}
