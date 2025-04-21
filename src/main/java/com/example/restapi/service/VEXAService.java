package com.example.restapi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.dto.TokenDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.repository.PostRepository;

@Service
public class VEXAService {

    private final PostRepository postRepository;


    public VEXAService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(PostDTO postDTO, String token) {
        User user = AuthService.getUserFromMap(token);
        if (user != null) {
            Post post = new Post(postDTO, user);
            postRepository.save(post);
            return post;
        }
        return null;
    }

    public List<Post> getPosts(String token) {
        if (AuthService.getUserFromMap(token) == null) {
            return null;
        }
        List<Post> posts = postRepository.findAll();
        posts.sort(Comparator.comparing(Post::getDate).reversed());
        return posts;
    }
    
    public List<Post> getPostsByUser(String token) {
        if (isValidToken(token)) {
        	System.out.println("se ejecuta\n");
            return postRepository.findByOwner(AuthService.getUserFromMap(token).getUsername());
        }
        return null;
    }

	private boolean isValidToken(String token) {
		return AuthService.getUserFromMap(token) != null;
	}
}