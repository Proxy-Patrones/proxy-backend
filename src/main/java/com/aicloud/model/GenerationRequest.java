package com.aicloud.model;

public class GenerationRequest {

    private final String userId;
    private final String prompt;

    public GenerationRequest(String userId, String prompt) {
        this.userId = userId;
        this.prompt = prompt;
    }

    public String getUserId() {
        return userId;
    }

    public String getPrompt() {
        return prompt;
    }
}
