package com.codex.tala;

public class Message {
    public static String SENT_BY_ME = "me";
    public static String SENT_BY_BOT = "bot";

    String message, sendby;

    public Message(String message, String sendby) {
        this.message = message;
        this.sendby = sendby;
    }

    public String getMessage() {
        return message;
    }

    public String getSendby() {
        return sendby;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSendby(String sendby) {
        this.sendby = sendby;
    }
}
