package com.zho.model;

public class KeywordAnalysis implements Comparable<KeywordAnalysis> {
    private final String keyword;
    private final long monthlySearches;
    private final long competitionIndex;
    private final double averageCpc;
    private final double score;
    private final int id; 
    private final String aiAnalysis;

    public KeywordAnalysis(String keyword, long monthlySearches, 
                          long competitionIndex, double averageCpc, 
                          double score, String aiAnalysis, int id) {
        this.keyword = keyword;
        this.monthlySearches = monthlySearches;
        this.competitionIndex = competitionIndex;
        this.averageCpc = averageCpc;
        this.score = score;
        this.aiAnalysis = aiAnalysis;
        this.id = id; 
    }

    @Override
    public int compareTo(KeywordAnalysis other) {
        // Sort by score in descending order
        return Double.compare(other.score, this.score);
    }
    
    // Getters
    public String getKeyword() { return keyword; }
    public long getMonthlySearches() { return monthlySearches; }
    public long getCompetitionIndex() { return competitionIndex; }
    public double getAverageCpc() { return averageCpc; }
    public double getScore() { return score; }
    public int getId() { return id; }
    public String getAiAnalysis() { return aiAnalysis; }
} 