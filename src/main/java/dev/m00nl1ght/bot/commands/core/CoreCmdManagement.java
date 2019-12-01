package dev.m00nl1ght.bot.commands.core;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;

public class CoreCmdManagement {

    public static void register(CoreCommand core) {
        core.addSubCommand(new Set(core.parent, "set"));
        core.addSubCommand(new Delete(core.parent, "delete"));
        core.addSubCommand(new Perm(core.parent, "perm"));
        core.addSubCommand(new Verbose(core.parent, "verbose"));
        core.addSubCommand(new Cooldown(core.parent, "cooldown"));
        core.addSubCommand(new Stats(core.parent, "stats"));
    }

    static class Set extends CoreSubCommand {

        protected Set(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String name = parser.nextParam();
            if (name.isEmpty()) {
                printUsage(parser);
                return;
            }
            String type = parser.nextParam();
            if (type.isEmpty()) {
                printUsage(parser);
                return;
            }
            String pattern = parser.readAll();
            parser.getParent().commandManager.createCommand(type, name, pattern);
            parser.sendResponse("Command !" + name + " set.");
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb set <command> <type> [text]");
        }

    }

    static class Delete extends CoreSubCommand {

        protected Delete(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String name = parser.nextParam();
            if (name.isEmpty()) {
                printUsage(parser);
                return;
            }
            parser.getParent().commandManager.deleteCommand(name);
            parser.sendResponse("Command !" + name + " deleted.");
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb delete <command>");
        }

    }

    static class Perm extends CoreSubCommand {

        protected Perm(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String name = parser.nextParam();
            if (name.isEmpty()) {
                printUsage(parser);
                return;
            }
            String val = parser.nextParam();
            if (val.isEmpty()) {
                printUsage(parser);
                return;
            }
            int v = -1;
            try {
                v = USER_LEVEL.valueOf(val.toUpperCase()).value;
            } catch (Exception e) {
            }
            if (v < 0) {
                try {
                    v = Integer.parseInt(val);
                } catch (Exception e) {
                }
            }
            if (v < 0) throw new CommandException("Invalid permission level: " + val);
            parser.getParent().commandManager.getCommandOrSub(name).setPerm(v);
            parser.sendResponse("Updated permissions for command !" + name);
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb perm <command> <value>");
        }

    }

    static class Verbose extends CoreSubCommand {

        protected Verbose(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String name = parser.nextParam();
            if (name.isEmpty()) {
                printUsage(parser);
                return;
            }
            String val = parser.nextParam();
            if (val.isEmpty()) {
                printUsage(parser);
                return;
            }
            boolean v = false;
            try {
                v = Boolean.parseBoolean(val.toLowerCase());
            } catch (Exception e) {
                throw new CommandException("Invalid boolean: " + val);
            }
            parser.getParent().commandManager.getCommandOrSub(name).setVerboseFeedback(v);
            parser.sendResponse("Updated verbose flag for command !" + name);
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb perm <command> <value>");
        }

    }

    static class Cooldown extends CoreSubCommand {

        protected Cooldown(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String name = parser.nextParam();
            if (name.isEmpty()) {
                printUsage(parser);
                return;
            }
            int v = parser.nextParamInt();
            parser.getParent().commandManager.getCommandOrSub(name).setCooldown(v * 1000);
            parser.sendResponse("Updated cooldown for command !" + name);
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb cooldown <command> <value>");
        }

    }

    static class Stats extends CoreSubCommand {

        protected Stats(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String name = parser.nextParam();
            if (name.isEmpty()) {
                printUsage(parser);
                return;
            }
            String stats = parser.getParent().commandManager.getCommandOrSub(name).printStats();
            parser.sendResponse("!" + name + " -> " + stats);
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb stats <command>");
        }

    }

}
