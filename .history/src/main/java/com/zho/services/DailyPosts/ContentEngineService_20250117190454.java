package com.zho.services.DailyPosts;

import com.zho.services.DailyPosts.KeywordResearchService;
import com.zho.services.DatabaseService;
import com.zho.model.BlogRequest;
import com.zho.model.KeywordAnalysis;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class ContentEngineService {
    private final KeywordResearchService keywordResearchService;
    private final AutoContentWorkflowService autoContentWorkflowService;
    private final DatabaseService databaseService;
    private ScheduledExecutorService scheduler;
    
    public ContentEngineService() {
        this.keywordResearchService = new KeywordResearchService();
        this.autoContentWorkflowService = new AutoContentWorkflowService();
        this.databaseService = new DatabaseService();
    }
    
    public void startContentEngine(BlogRequest request) {
        try {
            // 1. Perform keyword research
            List<KeywordAnalysis> keywords = keywordResearchService.getLongTailKeywords(request, 1000);
            
            // 2. Save to database
            //DB serivce. clear keywords. 
            //databaseService.saveKeywords(keywords);
            
            // 3. Start the automated content workflow
            startScheduledContentCreation();
            
            System.out.println("Content Engine started successfully");
            System.out.println("Found " + keywords.size() + " keywords");
            
        } catch (Exception e) {
            System.err.println("Error starting content engine: " + e.getMessage());
            throw new RuntimeException("Failed to start content engine", e);
        }
    }
    
    private void startScheduledContentCreation() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            () -> autoContentWorkflowService.processNextKeyword(),
            0, // initial delay
            24, // period
            TimeUnit.HOURS
        );
    }
    
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }
    
    public static void main(String[] args) {
        ContentEngineService service = new ContentEngineService();
        
        try {
            // Create a test BlogRequest
            BlogRequest request = new BlogRequest(
                "phase I ESA",
                "A blog about phase I environmental site assessment"
            );
            
            // Start the content engine
            service.startContentEngine(request);
            
            // Keep the application running to allow scheduled tasks to execute
            System.out.println("\nPress Ctrl+C to exit");
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (Exception e) {
            System.err.println("Error in main: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure proper shutdown
            service.shutdown();
        }
    }
}
