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

import org.jetbrains.annotations.Nullable;

public class ReflectionUtil {
  @Nullable
  public static Class getGrandCallerClass() {
    int stackFrameCount = 3;
    Class callerClass = findCallerClass(stackFrameCount);
    while (callerClass != null && callerClass.getClassLoader() == null) { // looks like a system class
      callerClass = findCallerClass(++stackFrameCount);
    }
    if (callerClass == null) {
      callerClass = findCallerClass(2);
    }
    return callerClass;
  }

  /**
   * Returns the class this method was called 'framesToSkip' frames up the caller hierarchy.
   *
   * NOTE:
   * <b>Extremely expensive!
   * Please consider not using it.
   * These aren't the droids you're looking for!</b>
   */
  @Nullable
  public static Class findCallerClass(int framesToSkip) {
    try {
      Class[] stack = MySecurityManager.INSTANCE.getStack();
      int indexFromTop = 1 + framesToSkip;
      return stack.length > indexFromTop ? stack[indexFromTop] : null;
    }
    catch (Exception e) {
//      LOG.warn(e);
      return null;
    }
  }

  private static class MySecurityManager extends SecurityManager {
    private static final MySecurityManager INSTANCE = new MySecurityManager();
    public Class[] getStack() {
      return getClassContext();
    }

  }

  private ReflectionUtil() { }
}
