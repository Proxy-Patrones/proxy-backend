package com.aicloud.model;

public class User {

    private final String id;
    private volatile Plan plan;

    public User(String id, Plan plan) {
        this.id = id;
        this.plan = plan;
    }

    public String getId() {
        return id;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }
}
