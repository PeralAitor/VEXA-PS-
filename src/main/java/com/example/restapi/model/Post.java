package com.example.restapi.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.example.restapi.dto.PostDTO;

import jakarta.persistence.ElementCollection;
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
    
    @ElementCollection
    private Set<String> likedBy = new HashSet<>();

    public Post() {
    }

    public Post(PostDTO postDTO, User owner) {
        this.content = postDTO.getContent();
        this.owner = owner.getUsername();
        this.date = new Date();
        this.likedBy = new HashSet<>();
    }

    public Post(String content, String owner, Date date) {
        this.content = content;
        this.owner = owner;
        this.date = date;
        this.likedBy = new HashSet<>();
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
    
    public String getFormattedDate() {
        if (date == null) return "Fecha no disponible"; // Evitar errores si la fecha es null
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
    
    public Set<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(Set<String> likedBy) {
        this.likedBy = likedBy;
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
