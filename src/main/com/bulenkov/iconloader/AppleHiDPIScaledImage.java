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

package com.bulenkov.iconloader;

import apple.awt.CImage;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Konstantin Bulenkov
 */
public class AppleHiDPIScaledImage {
  public static BufferedImage create(int width, int height, int imageType) {
    return new CImage.HiDPIScaledImage(width, height, imageType) {
      @Override
      protected void drawIntoImage(BufferedImage image, float scale) {
      }
    };
  }

  public static boolean is(Image image) {
    return image instanceof CImage.HiDPIScaledImage;
  }
}
