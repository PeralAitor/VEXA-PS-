package com.example.restapi.dto;

public class PostDTO {
    private String content;
    private String token;

    public PostDTO() {
    }

    public PostDTO(String content, String token) {
        this.content = content;
        this.token = token;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "PostDTO {" + "content='" + content + '\'' + '}';
    }
}
