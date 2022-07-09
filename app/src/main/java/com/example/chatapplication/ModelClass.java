package com.example.chatapplication;

public class ModelClass {

    String message;
    String from;

    public ModelClass() {
    }

    public ModelClass(String msgItSelf, String from) {
        this.message = msgItSelf;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
