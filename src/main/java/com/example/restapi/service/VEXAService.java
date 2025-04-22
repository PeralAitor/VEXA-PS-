package com.example.restapi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.dto.TokenDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.repository.PostRepository;

import jakarta.transaction.Transactional;

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
            return postRepository.findByOwner(AuthService.getUserFromMap(token).getUsername());
        }
        return null;
    }

	private boolean isValidToken(String token) {
		return AuthService.getUserFromMap(token) != null;
	}
	
	public Post updatePost(Post postDTO) {
		return postRepository.save(postDTO);
	}

	
	public Boolean deletePost(Post postDTO) {
	    if (postRepository.existsById(postDTO.getId())) {
	        postRepository.deleteById(postDTO.getId());
	        return true;
	    }
	    return false;
	}
}