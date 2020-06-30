package com.example.is1305project.model;

public class ChatList {
    private String id;
    private Long time;

    public ChatList(){

    }

    public ChatList(String id, Long time) {
        this.id = id;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

}
