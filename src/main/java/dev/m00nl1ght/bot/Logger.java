package dev.m00nl1ght.bot;

public class Logger {

    public static void log(String msg, Object... obj) {
        System.out.println("[INFO] "+String.format(msg, obj));
    }

    public static void warn(String msg, Object... obj) {
        System.out.println("[WARN] "+String.format(msg, obj));
    }

    public static void error(String msg, Object... obj) {
        System.out.println("[ERROR] "+String.format(msg, obj));
    }

}
