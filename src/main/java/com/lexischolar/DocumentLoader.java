package com.lexischolar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DocumentLoader {

    public static List<Document> load(String resourcePath) throws IOException {
        List<Document> docs = new ArrayList<>();
        try (InputStream in = DocumentLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException(resourcePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            int id = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|\\|", 2);
                if (parts.length < 2) continue;
                docs.add(new Document(id++, parts[0].trim(), parts[1].trim()));
            }
        }
        return docs;
    }
}