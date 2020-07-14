package com.example.is1305project.model;

import java.io.Serializable;

public class Chat implements Comparable<Chat> {
    private String id;
    private String sender;
    private String receiver;
    private String message;
    private Long time;
    private boolean isSeen;
    private boolean senderRemove;
    private boolean receiverRemove;

    public Chat(){

    }

    public Chat(String id, String sender, String receiver, String message, Long time, boolean isSeen
            , boolean senderRemove, boolean receiverRemove) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.isSeen = isSeen;
        this.senderRemove = senderRemove;
        this.receiverRemove = receiverRemove;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isSenderRemove() {
        return senderRemove;
    }

    public void setSenderRemove(boolean senderRemove) {
        this.senderRemove = senderRemove;
    }

    public boolean isReceiverRemove() {
        return receiverRemove;
    }

    public void setReceiverRemove(boolean receiverRemove) {
        this.receiverRemove = receiverRemove;
    }

    @Override
    public int compareTo(Chat o) {
        if(time - o.time > 0){
            return 0;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", message='" + message + '\'' +
                ", time=" + time +
                ", isSeen=" + isSeen +
                '}';
    }
}
