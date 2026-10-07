package com.lexischolar;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvertedIndex {

    // term -> (docId -> how many times the term appears in that doc)
    private final Map<String, Map<Integer, Integer>> postings = new HashMap<>();
    // docId -> total number of indexed tokens in that doc
    private final Map<Integer, Integer> docLengths = new HashMap<>();
    private int totalDocs = 0;

    public void addDocument(Document doc) {
        List<String> tokens = Tokenizer.tokenize(doc.title() + " " + doc.text());
        docLengths.put(doc.id(), tokens.size());
        totalDocs++;
        for (String token : tokens) {
            postings.computeIfAbsent(token, k -> new HashMap<>())
                    .merge(doc.id(), 1, Integer::sum);
        }
    }

    public Map<Integer, Integer> getPostings(String term) {
        return postings.getOrDefault(term, Collections.emptyMap());
    }

    public int getDocLength(int docId) { return docLengths.getOrDefault(docId, 1); }
    public int getTotalDocs() { return totalDocs; }
    public int getVocabularySize() { return postings.size(); }
}