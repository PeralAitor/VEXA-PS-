package com.example.restapi.model;

import java.util.Date;

import com.example.restapi.dto.PostDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "posts")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String owner;

    private String content;

    private Date date;

    public Post() {
    }

    public Post(PostDTO postDTO, User owner) {
        this.content = postDTO.getContent();
        this.owner = owner.getUsername();
        this.date = new Date();
    }

    public Post(String content, String owner, Date date) {
        this.content = content;
        this.owner = owner;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        return this.id == ((Post) obj).id;
    }

    @Override
    public String toString() {
        return "Post [owner=" + owner + ", content=" + content + ", date=" + date + "]";
    }
}
