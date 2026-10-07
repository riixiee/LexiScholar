package com.lexischolar;

import java.util.List;
import java.util.Map;

import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));

        long start = System.nanoTime();
        List<Document> docs = DocumentLoader.load("/docs/papers.txt");
        SearchEngine engine = new SearchEngine(docs);
        double buildMs = (System.nanoTime() - start) / 1_000_000.0;
        System.out.printf("Indexed %d documents, %d unique terms in %.2f ms%n",
                engine.getDocCount(), engine.getVocabularySize(), buildMs);

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(port);

        app.get("/health", ctx -> ctx.json(Map.of("status", "ok", "app", "LexiScholar")));
        app.get("/docs", ctx -> ctx.json(docs));

        app.get("/stats", ctx -> ctx.json(Map.of(
                "documents", engine.getDocCount(),
                "uniqueTerms", engine.getVocabularySize(),
                "indexBuildMs", buildMs)));

        // NEW: fetch one full document by id
        app.get("/doc/{id}", ctx -> {
            int id;
            try {
                id = Integer.parseInt(ctx.pathParam("id"));
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("error", "Invalid document id"));
                return;
            }
            final int target = id;
            Document found = docs.stream()
                    .filter(d -> d.id() == target)
                    .findFirst()
                    .orElse(null);
            if (found == null) {
                ctx.status(404).json(Map.of("error", "Document not found"));
                return;
            }
            ctx.json(found);
        });

        app.get("/search", ctx -> {
            String q = ctx.queryParam("q");
            if (q == null || q.isBlank()) {
                ctx.status(400).json(Map.of("error", "Missing query parameter 'q'"));
                return;
            }
            int k = 5;
            try {
                String kParam = ctx.queryParam("k");
                if (kParam != null) k = Math.max(1, Math.min(20, Integer.parseInt(kParam)));
            } catch (NumberFormatException ignored) {}

            long t0 = System.nanoTime();
            List<SearchResult> results = engine.search(q, k);
            double tookMs = (System.nanoTime() - t0) / 1_000_000.0;

            ctx.json(Map.of(
                    "query", q,
                    "tookMs", tookMs,
                    "count", results.size(),
                    "results", results));
        });
    }
}