package com.example.demo.model;


public class PushNotifyConf {

    private String title;
    private String body;
    private String icon;
    private String click_action;
    private String ttlInSeconds;

    public PushNotifyConf() {
    }

    public PushNotifyConf(String title, String body, String icon,
                          String click_action, String ttlInSeconds) {
        this.title = title;
        this.body = body;
        this.icon = icon;
        this.click_action = click_action;
        this.ttlInSeconds = ttlInSeconds;
    }

    public String getBody() {
        return body;
    }

    public String getClick_action() {
        return click_action;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getTtlInSeconds() {
        return ttlInSeconds;
    }

}