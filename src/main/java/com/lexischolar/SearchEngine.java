package com.lexischolar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class SearchEngine {

    private final InvertedIndex index = new InvertedIndex();
    private final Map<Integer, Document> docs = new HashMap<>();

    public SearchEngine(List<Document> documents) {
        for (Document d : documents) {
            docs.put(d.id(), d);
            index.addDocument(d);
        }
    }

    public List<SearchResult> search(String query, int k) {
        Map<Integer, Double> scores = new HashMap<>();
        Set<String> queryTerms = new LinkedHashSet<>(Tokenizer.tokenize(query));
        int n = index.getTotalDocs();

        for (String term : queryTerms) {
            Map<Integer, Integer> posting = index.getPostings(term);
            if (posting.isEmpty()) continue;

            // Smoothed IDF: never zero, never divides by zero
            double idf = Math.log((n + 1.0) / (posting.size() + 1.0)) + 1.0;

            for (Map.Entry<Integer, Integer> e : posting.entrySet()) {
                double tf = (double) e.getValue() / index.getDocLength(e.getKey());
                scores.merge(e.getKey(), tf * idf, Double::sum);
            }
        }

        // Min-heap of size k: the weakest of the current top-k sits at the root
        PriorityQueue<SearchResult> heap =
                new PriorityQueue<>(Comparator.comparingDouble(SearchResult::score));

        for (Map.Entry<Integer, Double> e : scores.entrySet()) {
            Document d = docs.get(e.getKey());
            double score = Math.round(e.getValue() * 10000.0) / 10000.0;
            heap.offer(new SearchResult(d.id(), d.title(), snippet(d.text()), score));
            if (heap.size() > k) heap.poll();   // drop the weakest
        }

        List<SearchResult> results = new ArrayList<>();
        while (!heap.isEmpty()) results.add(0, heap.poll());  // best first
        return results;
    }

    public int getDocCount() { return index.getTotalDocs(); }
    public int getVocabularySize() { return index.getVocabularySize(); }

    private String snippet(String text) {
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }
}