package com.lexischolar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tokenizer {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "the", "is", "are", "was", "were", "be", "been", "am",
        "and", "or", "but", "if", "of", "to", "in", "on", "at", "by", "for",
        "with", "from", "as", "that", "this", "these", "those", "it", "its",
        "we", "our", "us", "you", "your", "they", "their", "he", "she", "his", "her",
        "can", "will", "has", "have", "had", "not", "no", "so", "than", "then",
        "such", "into", "over", "between", "using", "use", "used", "which", "who"
    ));

    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        for (String raw : text.toLowerCase().split("[^a-z0-9]+")) {
            if (raw.length() > 1 && !STOP_WORDS.contains(raw)) {
                tokens.add(raw);
            }
        }
        return tokens;
    }
}