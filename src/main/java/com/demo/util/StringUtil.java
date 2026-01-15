package com.demo.util;

public class StringUtil {

    public static boolean hasText(String text) {
        return text != null && !text.isEmpty() && !text.isBlank();
    }
}
