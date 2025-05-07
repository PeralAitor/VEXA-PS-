package com.example.restapi.service;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VEXAServiceTest {

    @Mock
    private PostRepository postRepository;
    
    @Mock
    private UserRepository userRepository;

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
    
    @Test
    public void deleteUserSuccessTest() {
        // Configurar datos de prueba
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        
        // Configurar posts del usuario
        List<Post> userPosts = new ArrayList<>();
        userPosts.add(post);
        
        // Configurar posts con likes del usuario (uno con like y otro sin like)
        List<Post> allPosts = new ArrayList<>();
        
        // Post con like del usuario
        Post postWithLike = new Post();
        postWithLike.setId(2L);
        postWithLike.setLikedBy(new HashSet<>());
        postWithLike.getLikedBy().add(username);
        allPosts.add(postWithLike);
        
        // Post sin like del usuario
        Post postWithoutLike = new Post();
        postWithoutLike.setId(3L);
        postWithoutLike.setLikedBy(new HashSet<>());
        allPosts.add(postWithoutLike);
        
        // Configurar mocks
        when(postRepository.findByOwner(username)).thenReturn(userPosts);
        when(postRepository.findAll()).thenReturn(allPosts);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Ejecutar el método
        vexaService.deleteUser(userDTO);
        
        // Verificar interacciones
        verify(postRepository).findByOwner(username);
        verify(postRepository).findAll();
        
        // Verificar que solo se guarda el post que tenía like (branch true)
        verify(postRepository, times(1)).save(postWithLike);
        
        // Verificar que no se guarda el post que no tenía like (branch false)
        verify(postRepository, never()).save(postWithoutLike);
        
        verify(postRepository).deleteAll(userPosts);
        verify(userRepository).deleteById(username);
    }

    @Test
    public void likePostSuccessTest() {
        // Configurar datos de prueba
        String likerUsername = "anotherUser";
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Ejecutar el método
        Post result = vexaService.likePost(post.getId(), likerUsername);
        
        // Verificar resultados
        assertNotNull(result);
        assertTrue(result.getLikedBy().contains(likerUsername));
        verify(postRepository).save(post);
    }

    @Test
    public void likePostAlreadyLikedTest() {
        // Configurar datos de prueba
        String likerUsername = "anotherUser";
        post.getLikedBy().add(likerUsername); // Ya tiene el like
        
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        
        // Ejecutar el método
        Post result = vexaService.likePost(post.getId(), likerUsername);
        
        // Verificar resultados
        assertNotNull(result);
        assertEquals(post, result); // Debería devolver el mismo post sin cambios
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void likePostNotFoundTest() {
        // Configurar datos de prueba
        Long nonExistentId = 999L;
        when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // Ejecutar el método
        Post result = vexaService.likePost(nonExistentId, username);
        
        // Verificar resultados
        assertNull(result);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void unlikePostSuccessTest() {
        // Configurar datos de prueba
        String likerUsername = "anotherUser";
        post.getLikedBy().add(likerUsername); // Añadir like primero
        
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Ejecutar el método
        Post result = vexaService.unlikePost(post.getId(), likerUsername);
        
        // Verificar resultados
        assertNotNull(result);
        assertFalse(result.getLikedBy().contains(likerUsername));
        verify(postRepository).save(post);
    }

    @Test
    public void unlikePostNotLikedTest() {
        // Configurar datos de prueba
        String likerUsername = "anotherUser";
        // No añadir el like primero
        
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        
        // Ejecutar el método
        Post result = vexaService.unlikePost(post.getId(), likerUsername);
        
        // Verificar resultados
        assertNotNull(result);
        assertEquals(post, result); // Debería devolver el mismo post sin cambios
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void unlikePostNotFoundTest() {
        // Configurar datos de prueba
        Long nonExistentId = 999L;
        when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // Ejecutar el método
        Post result = vexaService.unlikePost(nonExistentId, username);
        
        // Verificar resultados
        assertNull(result);
        verify(postRepository, never()).save(any(Post.class));
    }
}