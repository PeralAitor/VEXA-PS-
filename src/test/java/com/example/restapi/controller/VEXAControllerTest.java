package com.example.restapi.controller;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.model.Post;
import com.example.restapi.service.VEXAService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VEXAControllerTest {

    @Mock
    private VEXAService vexaService;

    @InjectMocks
    private VEXAController vexaController;

    private PostDTO postDTO;
    private Post post;
    private String validToken = "validToken";
    private String invalidToken = "invalidToken";
    private String username = "testuser";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        postDTO = new PostDTO("Test Content", validToken);

        post = new Post();
        post.setId(1L);
        post.setContent("Test Content");
        post.setOwner(username);
    }

    @Test
    public void createPost_Success_ReturnsOk() {
        when(vexaService.createPost(postDTO, validToken)).thenReturn(post);

        ResponseEntity<Post> response = vexaController.createPost(postDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(post, response.getBody());
        verify(vexaService).createPost(postDTO, validToken);
    }

    @Test
    public void createPost_Unauthorized_ReturnsUnauthorized() {
        PostDTO invalidPostDTO = new PostDTO("Test Content", invalidToken);
        when(vexaService.createPost(invalidPostDTO, invalidToken)).thenReturn(null);

        ResponseEntity<Post> response = vexaController.createPost(invalidPostDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void getPosts_Success_ReturnsPosts() {
        List<Post> expectedPosts = new ArrayList<>();
        expectedPosts.add(post);
        when(vexaService.getPosts(validToken)).thenReturn(expectedPosts);

        ResponseEntity<List<Post>> response = vexaController.getPosts(validToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPosts, response.getBody());
        verify(vexaService).getPosts(validToken);
    }

    @Test
    public void getPostsByUser_Success_ReturnsUserPosts() {
        List<Post> expectedPosts = new ArrayList<>();
        expectedPosts.add(post);
        when(vexaService.getPostsByUser(validToken)).thenReturn(expectedPosts);

        ResponseEntity<List<Post>> response = vexaController.getPostsByUser(validToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPosts, response.getBody());
        verify(vexaService).getPostsByUser(validToken);
    }

    @Test
    public void updatePost_Success_ReturnsOk() {
        when(vexaService.updatePost(post)).thenReturn(post);

        ResponseEntity<Post> response = vexaController.updatePost(post);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(post, response.getBody());
        verify(vexaService).updatePost(post);
    }

    @Test
    public void updatePost_Failure_ReturnsUnauthorized() {
        when(vexaService.updatePost(post)).thenReturn(null);

        ResponseEntity<Post> response = vexaController.updatePost(post);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void deletePost_Success_ReturnsOk() {
        when(vexaService.deletePost(post)).thenReturn(true);

        ResponseEntity<Boolean> response = vexaController.deletePost(post);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(vexaService).deletePost(post);
    }

    @Test
    public void deletePost_Failure_ReturnsUnauthorized() {
        when(vexaService.deletePost(post)).thenReturn(false);

        ResponseEntity<Boolean> response = vexaController.deletePost(post);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody());
    }
}