package com.tenchael.cords.common.utils;

public class StringUtils {

    public static boolean isBlank(CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return true;
        }
        return false;
    }

}
