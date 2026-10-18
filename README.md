LexiScholar -

A small search engine for research papers. It indexes paper abstracts with an inverted index and ranks results using TF-IDF implementation. 

Features:

1. Searches paper abstracts by keyword and returns ranked results with relevance scores.

2. Ranks with a custom TF-IDF, using a PriorityQueue (heap) to pull out the top results.

3. Keeps the whole index in memory, so queries come back in a few milliseconds.

4. Removes stop words and tokenize text before indexing.

5. Full-document view for search result.

Future work:

1. Uploading `.txt` documents and indexing them at runtime.

2. Phrase queries and boosting matches in titles.
