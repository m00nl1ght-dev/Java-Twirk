package dev.m00nl1ght.bot;

public class CommandException extends RuntimeException {

    public CommandException(String s) {
        super(s);
    }

    public CommandException(String s, Throwable t) {
        super(s, t);
    }

}
