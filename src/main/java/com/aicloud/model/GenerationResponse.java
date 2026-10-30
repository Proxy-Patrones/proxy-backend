package com.aicloud.model;

public class GenerationResponse {

    private final String text;
    private final int promptTokens;
    private final int completionTokens;
    private final long processingTimeMs;

    public GenerationResponse(String text, int promptTokens, int completionTokens, long processingTimeMs) {
        this.text = text;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.processingTimeMs = processingTimeMs;
    }

    public String getText() {
        return text;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public String getModel() {
        return "mock-gpt-1";
    }
}
