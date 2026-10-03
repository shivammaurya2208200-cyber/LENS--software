package com.lens;

import java.io.File;
import java.nio.file.Path;

public class SearchResult implements Comparable<SearchResult> {
    private File file;
    private int relevance;

    public SearchResult(File file, int relevance) {
        this.file = file;
        this.relevance = relevance;
    }

    public File getFile() { return file; }
    public int getRelevance() { return relevance; }

    @Override
    public int compareTo(SearchResult o) {
        return Integer.compare(o.relevance, this.relevance); // Descending order
    }
}
