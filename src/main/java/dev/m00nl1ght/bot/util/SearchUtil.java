package dev.m00nl1ght.bot.util;

import java.util.Map;
import java.util.Optional;

public class SearchUtil {

    public static <T> Optional<T> findMatch(Map<String, T> map, String query, double threshold) {
        final T ret = map.get(query);
        if (ret != null) return Optional.of(ret);
        double bestV = 0D; String bestO = null;
        for (String s : map.keySet()) {
            double d = similarity(s, query);
            if (d > threshold && d > bestV) {
                bestO = s; bestV = d;
            }
        }
        return bestO == null ? Optional.empty() : Optional.ofNullable(map.get(bestO));
    }

    public static Optional<String> findMatchKey(Map<String, ?> map, String query, double threshold) {
        final Object ret = map.get(query);
        if (ret != null) return Optional.of(query);
        double bestV = 0D; String bestO = null;
        for (String s : map.keySet()) {
            double d = similarity(s, query);
            if (d > threshold && d > bestV) {
                bestO = s; bestV = d;
            }
        }
        return bestO == null ? Optional.empty() : Optional.ofNullable(bestO);
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {longer = s2; shorter = s1;}
        int longerLength = longer.length();
        if (longerLength == 0) return 1.0;
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(CharSequence left, CharSequence right) {
        int n = left.length(); // length of left
        int m = right.length(); // length of right

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        if (n > m) {
            // swap the input strings to consume less memory
            final CharSequence tmp = left;
            left = right;
            right = tmp;
            n = m;
            m = right.length();
        }

        final int[] p = new int[n + 1];

        // indexes into strings left and right
        int i; // iterates through left
        int j; // iterates through right
        int upperLeft;
        int upper;

        char rightJ; // jth character of right
        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            upperLeft = p[0];
            rightJ = right.charAt(j - 1);
            p[0] = j;

            for (i = 1; i <= n; i++) {
                upper = p[i];
                cost = left.charAt(i - 1) == rightJ ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upperLeft + cost);
                upperLeft = upper;
            }
        }

        return p[n];
    }

}
