package com.example.is1305project.model;

import java.io.Serializable;

public class Chat implements Comparable<Chat> {
    private String sender;
    private String receiver;
    private String message;
    private Long time;
    private boolean isSeen;

    public Chat(){

    }

    public Chat(String sender, String receiver, String message, Long time, boolean isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.isSeen = isSeen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isIsSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    @Override
    public int compareTo(Chat o) {
        if(time - o.time > 0){
            return 0;
        }
        return 1;
    }
}
