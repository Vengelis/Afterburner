/**
 * Created by Vengelis_.
 * Date: 6/17/2023
 * Time: 2:53 PM
 * Project: VelocityReloaded
 */

package fr.vengelis.afterburner.utils;

import java.util.Arrays;
import java.util.List;

public class StringUtils {

    public static final String EMPTY = "";

    public static String chop(String str) {
        if (str == null) {
            return null;
        }
        int strLen = str.length();
        if (strLen < 2) {
            return EMPTY;
        }
        int lastIdx = strLen - 1;
        String ret = str.substring(0, lastIdx);
        char last = str.charAt(lastIdx);
        if (last == CharUtils.LF) {
            if (ret.charAt(lastIdx - 1) == CharUtils.CR) {
                return ret.substring(0, lastIdx - 1);
            }
        }
        return ret;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static Boolean isValid(String string) {
        List<String> splitted = Arrays.asList(string.split(" "));
        for(String s : splitted) {
            for(PSBWords bw : PSBWords.values()) {
                if(s.toUpperCase().contains(bw.toString())) return false;
            }
        }
        return true;
    }

    public static Boolean verify16letterMax(String string) {
        char[] str = string.toCharArray();
        return str.length <= 16;
    }

    public static Boolean verify10letterMax(String string) {
        char[] str = string.toCharArray();
        return str.length <= 10;
    }

    public static Boolean verify5letterMax(String string) {
        char[] str = string.toCharArray();
        return str.length <= 5;
    }

    public static String colorCleanner(String string) {
        String rtn = string.replace("&0", "");
        rtn = rtn.replace("&1", "");
        rtn = rtn.replace("&2", "");
        rtn = rtn.replace("&3", "");
        rtn = rtn.replace("&4", "");
        rtn = rtn.replace("&5", "");
        rtn = rtn.replace("&6", "");
        rtn = rtn.replace("&7", "");
        rtn = rtn.replace("&8", "");
        rtn = rtn.replace("&9", "");
        rtn = rtn.replace("&a", "");
        rtn = rtn.replace("&b", "");
        rtn = rtn.replace("&c", "");
        rtn = rtn.replace("&d", "");
        rtn = rtn.replace("&e", "");
        rtn = rtn.replace("&f", "");
        rtn = rtn.replace("&o", "");
        rtn = rtn.replace("&l", "");
        rtn = rtn.replace("&n", "");
        rtn = rtn.replace("&r", "");
        rtn = rtn.replace("&k", "");
        rtn = rtn.replace("&m", "");
        return rtn;
    }

    public enum PSBWords {
        ADMIN,
        ADMINISTRATEUR,
        MODERATEUR,
        MODO,
        MOD,
        STAFF,
        DEV,
        HELP,
        HELPEUR,
        HELPER,
        GD,
        BUILD
    }

}
