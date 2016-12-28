package com.mucommander.commons.file;

import java.lang.annotation.*;

/**
 * @author Maxence Bernard
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UnsupportedFileOperation {
}
