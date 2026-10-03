package com.lens;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchEngine {

    public enum TimeLimit {
        ANY, PAST_WEEK, PAST_MONTH, PAST_YEAR
    }

    public List<SearchResult> search(String query, File startDirectory, TimeLimit timeLimit, String typeLimit, int maxPages) {
        List<SearchResult> results = new ArrayList<>();
        if (startDirectory == null || !startDirectory.exists()) return results;
        
        Instant now = Instant.now();
        Instant limitDate;
        
        switch (timeLimit) {
            case PAST_WEEK: limitDate = now.minus(7, ChronoUnit.DAYS); break;
            case PAST_MONTH: limitDate = now.minus(30, ChronoUnit.DAYS); break;
            case PAST_YEAR: limitDate = now.minus(365, ChronoUnit.DAYS); break;
            default: limitDate = Instant.MIN; break;
        }

        try {
            Files.walkFileTree(startDirectory.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        File file = path.toFile();
                        
                        // Check Time Limit
                        Instant lastModified = attrs.lastModifiedTime().toInstant();
                        if (lastModified.isBefore(limitDate)) {
                            return FileVisitResult.CONTINUE;
                        }

                        String fileName = file.getName().toLowerCase();
                        
                        // Check File Type Limit
                        if (!typeLimit.equals("All Types")) {
                            if (typeLimit.equals("PDF") && !fileName.endsWith(".pdf")) return FileVisitResult.CONTINUE;
                            if (typeLimit.equals("DOCX") && !fileName.endsWith(".docx")) return FileVisitResult.CONTINUE;
                            if (typeLimit.equals("TXT") && !fileName.endsWith(".txt")) return FileVisitResult.CONTINUE;
                        }

                        int count = 0;
                        if (fileName.endsWith(".pdf")) {
                            count = searchPdf(file, query, maxPages);
                        } else if (fileName.endsWith(".docx")) {
                            count = searchDocx(file, query);
                        } else if (fileName.endsWith(".txt")) {
                            count = searchTxt(file, query);
                        }
                        
                        if (count > 0) {
                            results.add(new SearchResult(file, count));
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE; // Skip locked system files
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Collections.sort(results);
        return results;
    }

    private int searchPdf(File file, String query, int maxPages) {
        int count = 0;
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            
            // Limit the number of pages to speed up search!
            if (maxPages > 0) {
                stripper.setEndPage(Math.min(maxPages, document.getNumberOfPages()));
            }
            
            String text = stripper.getText(document).toLowerCase();
            count = countOccurrences(text, query.toLowerCase());
        } catch (Exception e) { }
        return count;
    }

    private int searchDocx(File file, String query) {
        int count = 0;
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText().toLowerCase();
            count = countOccurrences(text, query.toLowerCase());
        } catch (Exception e) { }
        return count;
    }

    private int searchTxt(File file, String query) {
        int count = 0;
        try {
            String text = new String(Files.readAllBytes(file.toPath())).toLowerCase();
            count = countOccurrences(text, query.toLowerCase());
        } catch (Exception e) { }
        return count;
    }

    private int countOccurrences(String text, String query) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(query, idx)) != -1) {
            count++;
            idx += query.length();
        }
        return count;
    }
}
