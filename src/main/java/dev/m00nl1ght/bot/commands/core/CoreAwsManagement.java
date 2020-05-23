package dev.m00nl1ght.bot.commands.core;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.answers.Answer;
import dev.m00nl1ght.bot.answers.AnswersManager;
import dev.m00nl1ght.bot.answers.Trigger;

public class CoreAwsManagement {

    public static void register(CoreCommand core) {
        core.addSubCommand(new Set(core.parent, "awset"));
        core.addSubCommand(new Triggers(core.parent, "awtriggers"));
        core.addSubCommand(new Delete(core.parent, "awdelete"));
        core.addSubCommand(new Cooldown(core.parent, "awcooldown"));
        core.addSubCommand(new Stats(core.parent, "awstats"));
        core.addSubCommand(new Mode(core.parent, "awmode"));
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
            String response = parser.readAll();
            if (response.trim().isEmpty()) {
                printUsage(parser);
                return;
            }
            parser.getParent().answersManager.getOrCreateAnswer(name, response);
            parser.sendResponse("Answer " + name + " set.");
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb awset <name> [response]");
        }

    }

    static class Triggers extends CoreSubCommand {

        protected Triggers(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String name = parser.nextParam();
            if (name.isEmpty()) {
                printUsage(parser);
                return;
            }

            final Answer answer = parser.getParent().answersManager.getAnswer(name);
            String next = parser.nextParam();
            boolean remove = false;
            if (next.equals("-")) remove = true;
            else if (!next.equals("+")) {
                answer.getTriggers().clear();
                if (!next.isEmpty()) answer.getTriggers().add(Trigger.fromPattern(next));
            }

            while (!(next = parser.nextParam()).isEmpty()) {
                if (remove) answer.getTriggers().remove(next.toLowerCase());
                else answer.getTriggers().add(Trigger.fromPattern(next));
            }

            parser.sendResponse("Updated triggers for answer " + name);
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb awtriggers <name> [+|-] [triggers]");
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
            parser.getParent().answersManager.deleteAnswer(name);
            parser.sendResponse("Answer " + name + " deleted.");
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb awdelete <name>");
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
            parser.getParent().answersManager.getAnswer(name).setCooldown(v * 1000);
            parser.sendResponse("Updated cooldown for answer " + name);
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb awcooldown <name> <value>");
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
            String stats = parser.getParent().answersManager.getAnswer(name).printStats();
            parser.sendResponse("Answer " + name + " -> " + stats);
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb awstats <name>");
        }

    }

    static class Mode extends CoreSubCommand {

        protected Mode(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String mode = parser.nextParam();
            if (mode.isEmpty()) {
                printUsage(parser);
                return;
            }

            try {
                final AnswersManager.Mode m = AnswersManager.Mode.valueOf(mode.toUpperCase());
                parser.getParent().answersManager.setMode(m);
                parser.sendResponse("AnswerEngine mode set to " + m + ".");
            } catch (IllegalArgumentException e) {
                parser.sendResponse("Error: No such mode exists: " + mode);
            }
        }

        private void printUsage(CommandParser parser) {
            parser.sendResponse("Usage: !mb awmode <off|text|mention>");
        }

    }

}
