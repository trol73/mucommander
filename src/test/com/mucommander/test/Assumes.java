package com.mucommander.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.testng.SkipException;

public class Assumes {
    public static <T> void assumeThat(T actual, Matcher<? super T> matcher) {
        assumeThat("", actual, matcher);
    }
 
    public static <T> void assumeThat(String reason, T actual, Matcher<? super T> matcher) {
        if (!matcher.matches(actual)) {
            Description description = new StringDescription();
            description.appendText(reason)
                    .appendText("\nExpected: ")
                    .appendDescriptionOf(matcher)
                    .appendText("\n     but: ");
            matcher.describeTo(description);
            description.appendValue(actual);
            throw new SkipException(description.toString());
        }
    }
 
    public static void assumeThat(String reason, boolean assertion) {
        if (!assertion) {
            throw new SkipException(reason+": false. Tests are ignored.");
        }
    }
}