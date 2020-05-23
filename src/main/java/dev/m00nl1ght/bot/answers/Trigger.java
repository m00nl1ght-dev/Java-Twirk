package dev.m00nl1ght.bot.answers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface Trigger {

    boolean test(String msg, int q);

    String pattern();

    public static Trigger fromPattern(String pattern) {
        String[] kw = pattern.toLowerCase().split("&");
        if (kw.length == 1) {
            return new Simple(kw[0]);
        } else {
            return new Multi(Arrays.asList(kw));
        }
    }

    public static class Simple implements Trigger {

        private final String keyword;

        public Simple(String keyword) {
            this.keyword = keyword;
        }

        @Override
        public boolean test(String msg, int q) {
            final int p = msg.indexOf(keyword);
            return p >= 0 && p < q;
        }

        @Override
        public String pattern() {
            return keyword;
        }

    }

    public static class Multi implements Trigger {

        private final List<String> keywords;

        public Multi(List<String> keywords) {
            this.keywords = keywords;
        }

        @Override
        public boolean test(String msg, int q) {
            for (final String keyword : keywords) {
                final int p = msg.indexOf(keyword);
                if (p < 0 || p >= q) return false;
            }
            return true;
        }

        @Override
        public String pattern() {
            return keywords.stream().collect(Collectors.joining("&"));
        }

    }

}
