package com.samuraiiway.restrepository.util;

public class StringUtil {
    private StringUtil() {}

    public static String getBeanName(String className) {
        String[] names = className.split("\\.");
        String name = names[names.length - 1];
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }
}
