package com.example.restapi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.dto.TokenDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class VEXAService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public VEXAService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }
    /*
     * Crea un nuevo post en el sistema.
     */
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
	
	public void deleteUser(UserDTO userDTO) {
		// Eliminar likes del usuario en todos los posts
	    List<Post> allPosts = postRepository.findAll();
	    for (Post post : allPosts) {
	        if (post.getLikedBy().remove(userDTO.getUsername())) {
	            postRepository.save(post);
	        }
	    }
	    
	    // Eliminar posts del usuario
	    List<Post> userPosts = postRepository.findByOwner(userDTO.getUsername());
	    postRepository.deleteAll(userPosts);
	    
	    // Eliminar el usuario
	    userRepository.deleteById(userDTO.getUsername());
    }
	
	public Post likePost(Long postId, String username) {
	    Optional<Post> postOpt = postRepository.findById(postId);
	    if (postOpt.isEmpty()) return null;
	    Post post = postOpt.get();
	    if (post.getLikedBy().add(username)) {
	        return postRepository.save(post);
	    }
	    return post;
	}

	public Post unlikePost(Long postId, String username) {
	    Optional<Post> postOpt = postRepository.findById(postId);
	    if (postOpt.isEmpty()) return null;
	    Post post = postOpt.get();
	    if (post.getLikedBy().remove(username)) {
	        return postRepository.save(post);
	    }
	    return post;
	}
}