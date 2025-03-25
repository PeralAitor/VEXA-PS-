package com.example.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.restapi.model.Post;
import com.example.restapi.model.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByOwner(User owner);
    
}
