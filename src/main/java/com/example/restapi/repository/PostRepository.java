package com.example.restapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.restapi.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByOwner(String owner);
    Optional<Post> findById(Long id);
}
