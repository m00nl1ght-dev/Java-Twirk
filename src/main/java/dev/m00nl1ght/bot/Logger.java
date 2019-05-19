package dev.m00nl1ght.bot;

public class Logger {

    public static void log(String msg, Object... obj) {
        System.out.println("[INFO] " + (obj.length > 0 ? String.format(msg, obj) : msg));
    }

    public static void warn(String msg, Object... obj) {
        System.out.println("[WARN] " + (obj.length > 0 ? String.format(msg, obj) : msg));
    }

    public static void error(String msg, Object... obj) {
        System.out.println("[ERROR] " + (obj.length > 0 ? String.format(msg, obj) : msg));
    }

}
