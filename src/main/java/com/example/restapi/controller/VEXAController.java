package com.example.restapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.dto.TokenDTO;
import com.example.restapi.model.Post;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.service.AuthService;
import com.example.restapi.service.VEXAService;


@RestController
@RequestMapping("/vexa")
public class VEXAController {

    private final VEXAService vexaService;
    private final PostRepository postRepository;

    public VEXAController(VEXAService vexaService,PostRepository postRepository) {
        this.vexaService = vexaService;
        this.postRepository = postRepository;
    }

    @PostMapping("/post")
    public ResponseEntity<Post> createPost(@RequestBody PostDTO postDTO) {
        Post response = vexaService.createPost(postDTO, postDTO.getToken());
        if(response != null) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getPosts(@RequestParam String token) {
        List<Post> response = vexaService.getPosts(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    	
    @GetMapping("/posts/user")
    public ResponseEntity<List<Post>> getPostsByUser(@RequestParam String token) {
        List<Post> response = vexaService.getPostsByUser(token);
        System.out.println("Response: " + response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
}