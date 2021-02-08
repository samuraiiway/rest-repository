package com.samuraiiway.restrepository.util;

public class StringUtil {
    private StringUtil() {}

    public static String classNameToCamelCase(String className) {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }
}
