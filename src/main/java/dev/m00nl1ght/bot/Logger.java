package dev.m00nl1ght.bot;

import sun.security.action.GetPropertyAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static FileWriter outWriter;
    private static File outFile;
    private static final SimpleDateFormat logFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
    private static final String lineSeperator = AccessController.doPrivileged(new GetPropertyAction("line.separator"));

    public static void log(String msg, Object... obj) {
        out("[INFO] " + (obj.length > 0 ? String.format(msg, obj) : msg));
    }

    public static void warn(String msg, Object... obj) {
        out("[WARN] " + (obj.length > 0 ? String.format(msg, obj) : msg));
    }

    public static void error(String msg, Object... obj) {
        out("[ERROR] " + (obj.length > 0 ? String.format(msg, obj) : msg));
    }

    private static void out(String str) {
        System.out.println(str);
        if (outWriter != null) {
            try {
                outWriter.write(logFormat.format(new Date()) + " " + str + lineSeperator);
                outWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                outWriter = null;
            }
        }
    }

    public static void create(File file) {
        outFile = file;
        try {
            outWriter = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException("failed to open log file", e);
        }
    }

    public static void dispose() {
        if (outWriter != null) {
            try {
                outWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                outWriter = null;
            }
        }
    }

    public static void cleanLog() {
        try {
            outWriter.close();
            outFile.delete();
            outWriter = new FileWriter(outFile);
        } catch (Exception e) {
            throw new RuntimeException("failed to clean log file", e);
        }
    }

}
