package com.example.restapi.service;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.repository.PostRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VEXAServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private VEXAService vexaService;

    private PostDTO postDTO;
    private Post post;
    private User user;
    private String validToken = "validToken";
    private String invalidToken = "invalidToken";
    private String username = "testuser";

    private static MockedStatic<AuthService> mockedAuthService;

    @BeforeAll
    public static void init() {
        mockedAuthService = mockStatic(AuthService.class);
    }

    @AfterAll
    public static void close() {
        mockedAuthService.close();
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUsername(username);
        user.setPassword("password");

        postDTO = new PostDTO("Test Content", validToken);

        post = new Post();
        post.setId(1L);
        post.setContent("Test Content");
        post.setOwner(username);

        // Resetear los mocks estáticos antes de cada test
        mockedAuthService.reset();
        
        // Configurar comportamiento por defecto
        mockedAuthService.when(() -> AuthService.getUserFromMap(validToken))
                        .thenReturn(user);
        mockedAuthService.when(() -> AuthService.getUserFromMap(invalidToken))
                        .thenReturn(null);
    }

    @Test
    public void createPostSuccessTest() {
        Post result = vexaService.createPost(postDTO, validToken);

        assertNotNull(result);
        assertEquals(postDTO.getContent(), result.getContent());
        assertEquals(username, result.getOwner());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    public void createPostFailureInvalidTokenTest() {
        PostDTO invalidPostDTO = new PostDTO("Test Content", invalidToken);
        Post result = vexaService.createPost(invalidPostDTO, invalidToken);

        assertNull(result);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void getPostsSuccessTest() {
        List<Post> expectedPosts = new ArrayList<>();
        expectedPosts.add(post);

        when(postRepository.findAll()).thenReturn(expectedPosts);

        List<Post> result = vexaService.getPosts(validToken);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(post.getContent(), result.get(0).getContent());
    }

    @Test
    public void getPostsFailureInvalidTokenTest() {
        List<Post> result = vexaService.getPosts(invalidToken);

        assertNull(result);
        verify(postRepository, never()).findAll();
    }

    @Test
    public void getPostsByUserSuccessTest() {
        List<Post> expectedPosts = new ArrayList<>();
        expectedPosts.add(post);

        when(postRepository.findByOwner(username)).thenReturn(expectedPosts);

        List<Post> result = vexaService.getPostsByUser(validToken);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(post.getContent(), result.get(0).getContent());
        assertEquals(username, result.get(0).getOwner());
    }

    @Test
    public void getPostsByUserFailureInvalidTokenTest() {
        List<Post> result = vexaService.getPostsByUser(invalidToken);

        assertNull(result);
        verify(postRepository, never()).findByOwner(anyString());
    }

    @Test
    public void updatePostSuccessTest() {
        when(postRepository.save(post)).thenReturn(post);

        Post result = vexaService.updatePost(post);

        assertNotNull(result);
        assertEquals(post.getId(), result.getId());
        verify(postRepository).save(post);
    }

    @Test
    public void deletePostSuccessTest() {
        when(postRepository.existsById(post.getId())).thenReturn(true);

        Boolean result = vexaService.deletePost(post);

        assertTrue(result);
        verify(postRepository).deleteById(post.getId());
    }

    @Test
    public void deletePostFailurePostNotExistsTest() {
        when(postRepository.existsById(post.getId())).thenReturn(false);

        Boolean result = vexaService.deletePost(post);

        assertFalse(result);
        verify(postRepository, never()).deleteById(anyLong());
    }
}