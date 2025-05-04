package com.example.restapi.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class UserManagerTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserManager userManager;

    private final String TEST_TOKEN = "test-token";
    private final String ADMIN_TOKEN = "admin";

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        ReflectionTestUtils.setField(userManager, "restTemplate", restTemplate);
    }

    // Tests para métodos de vista

    @Test
    void registerForm_ShouldReturnIndexView() {
        assertEquals("index", userManager.registerForm(new User()));
    }

    @Test
    void showRegistrationForm_ShouldReturnRegistrationView() {
        assertEquals("registration", userManager.showRegistrationForm(model));
    }

    @Test
    void showLoginForm_ShouldReturnLoginView() {
        assertEquals("login", userManager.showLoginForm(model));
    }

    @Test
    void post_ShouldReturnPostView() {
        assertEquals("post", userManager.post(model));
    }

    // Tests para register()

    @Test
    void register_SuccessfulRegistration_ShouldAddSuccessMessage() {
        User user = new User("user", "pass", "name", "surname", 25);
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(User.class)))
            .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));

        String result = userManager.register("user", "pass", "name", "surname", 25, model);

        assertEquals("registration", result);
        verify(model).addAttribute("successMessage", "User registered successfully");
    }

    @Test
    void register_FailedRegistration_ShouldAddErrorMessage() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(User.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

        String result = userManager.register("user", "pass", "name", "surname", 25, model);

        assertEquals("registration", result);
        verify(model).addAttribute("errorMessage", "User already exists");
    }

    @Test
    void register_OtherHttpError_ShouldThrowException() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(User.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> 
            userManager.register("user", "pass", "name", "surname", 25, model));
    }

    // Tests para login()

    @Test
    void login_AdminSuccess_ShouldReturnAdminView() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>(ADMIN_TOKEN, HttpStatus.OK));
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenReturn(Collections.singletonList(new Post()));
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                isNull(), 
                any(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity<>(Collections.singletonList(new User()), HttpStatus.OK));

        String result = userManager.login("admin", "admin", model);

        assertEquals("admin", result);
        verify(model).addAttribute("token", ADMIN_TOKEN);
        verify(model).addAttribute("successMessage", "Admin logged in successfully");
    }

    @Test
    void login_UserSuccess_ShouldReturnPostsView() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>(TEST_TOKEN, HttpStatus.OK));
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenReturn(Collections.singletonList(new Post()));

        String result = userManager.login("user", "pass", model);

        assertEquals("posts", result);
        verify(model).addAttribute("token", TEST_TOKEN);
        verify(model).addAttribute("successMessage", "User logged in successfully");
    }

    @Test
    void login_Failure_ShouldReturnLoginViewWithError() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        String result = userManager.login("user", "wrongpass", model);

        assertEquals("login", result);
        verify(model).addAttribute("errorMessage", "User not found");
    }

    // Tests para logout()

    @Test
    void logout_Success_ShouldClearTokenAndReturnIndex() {
        userManager.token = TEST_TOKEN;
        when(restTemplate.postForEntity(anyString(), eq(TEST_TOKEN), eq(Boolean.class)))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        String result = userManager.logout(model);

        assertEquals("index", result);
        assertNull(userManager.token);
        verify(model).addAttribute("token", null);
        verify(model).addAttribute("successMessage", "User logged out successfully");
    }

    @Test
    void logout_Failure_ShouldReturnIndexWithError() {
        userManager.token = TEST_TOKEN;
        when(restTemplate.postForEntity(anyString(), eq(TEST_TOKEN), eq(Boolean.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        String result = userManager.logout(model);

        assertEquals("index", result);
        verify(model).addAttribute("errorMessage", "User not found");
    }

    // Tests para getPosts()

    @Test
    void getPosts_WithoutToken_ShouldReturnPostsView() {
        userManager.token = null;
        String result = userManager.getPosts(model);
        assertEquals("posts", result);
        verify(model, never()).addAttribute(eq("posts"), any());
    }
    // Tests para createPost()

    @Test
    void createPost_Success_ShouldReturnPostViewWithSuccess() {
        userManager.token = TEST_TOKEN;
        when(restTemplate.postForEntity(anyString(), any(PostDTO.class), eq(Post.class)))
            .thenReturn(new ResponseEntity<>(new Post(), HttpStatus.OK));

        String result = userManager.createPost("Test content", model);

        assertEquals("post", result);
        verify(model).addAttribute("successMessage", "Post created successfully");
    }

    @Test
    void createPost_Failure_ShouldReturnPostViewWithError() {
        userManager.token = TEST_TOKEN;
        when(restTemplate.postForEntity(anyString(), any(PostDTO.class), eq(Post.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        String result = userManager.createPost("Test content", model);

        assertEquals("post", result);
        verify(model).addAttribute("errorMessage", "Post not created");
    }

    @Test
    void createPost_NoToken_ShouldReturnIndex() {
        userManager.token = null;
        String result = userManager.createPost("Test content", model);
        assertEquals("index", result);
    }

    // Tests para updatePost() (GET)

    @Test
    void updatePost_Get_ShouldReturnEditPostView() {
        userManager.token = TEST_TOKEN;
        Post testPost = new Post();
        testPost.setId(1L);
        testPost.setContent("Old content");
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), 
            any(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity<>(Collections.singletonList(testPost), HttpStatus.OK));

        String result = userManager.updatePost(1L, model);

        assertEquals("editPost", result);
        verify(model).addAttribute("post", testPost);
        verify(model).addAttribute("id", 1L);
    }

    @Test
    void updatePost_Get_PostNotFound_ShouldReturnIndex() {
        userManager.token = TEST_TOKEN;
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), 
            any(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK));

        String result = userManager.updatePost(1L, model);

        assertEquals("index", result);
    }

    // Tests para addAttributes()

    @Test
    void addAttributes_ShouldAddCurrentUrlAndToken() {
        when(request.getRequestURI()).thenReturn("/test");
        userManager.token = TEST_TOKEN;
        
        userManager.addAttributes(model, request);
        
        verify(model).addAttribute(eq("currentUrl"), anyString());
        verify(model).addAttribute("token", TEST_TOKEN);
    }

    // Tests para métodos de servicio

    @Test
    void register_ShouldReturnTrueOnSuccess() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(User.class)))
            .thenReturn(new ResponseEntity<>(new User(), HttpStatus.OK));
        
        assertTrue(userManager.register(new User("user", "pass", "name", "surname", 25)));
    }

    @Test
    void login_ShouldReturnTokenOnSuccess() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>(TEST_TOKEN, HttpStatus.OK));
        
        assertEquals(TEST_TOKEN, userManager.login(new User("user", "pass")));
    }

    @Test
    void logout_ShouldReturnTrueOnSuccess() {
        when(restTemplate.postForEntity(anyString(), eq(TEST_TOKEN), eq(Boolean.class)))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));
        
        assertTrue(userManager.logout(TEST_TOKEN));
    }

    @Test
    void getPosts_ShouldReturnPostsList() {
        List<Post> expectedPosts = Arrays.asList(new Post(), new Post());
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenReturn(expectedPosts);
        
        assertEquals(expectedPosts, userManager.getPosts(TEST_TOKEN));
    }

    @Test
    void getPostsOwner_ShouldReturnPostsList() {
        List<Post> expectedPosts = Arrays.asList(new Post(), new Post());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), 
            any(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity<>(expectedPosts, HttpStatus.OK));
        
        assertEquals(expectedPosts, userManager.getPostsOwner(TEST_TOKEN));
    }

    @Test
    void createPost_ShouldReturnCreatedPost() {
        Post expectedPost = new Post();
        when(restTemplate.postForEntity(anyString(), any(PostDTO.class), eq(Post.class)))
            .thenReturn(new ResponseEntity<>(expectedPost, HttpStatus.OK));
        
        assertEquals(expectedPost, userManager.createPost(new PostDTO("content", TEST_TOKEN)));
    }

    @Test
    void updatePost_ShouldReturnUpdatedPost() {
        Post expectedPost = new Post();
        when(restTemplate.postForEntity(anyString(), any(Post.class), eq(Post.class)))
            .thenReturn(new ResponseEntity<>(expectedPost, HttpStatus.OK));
        
        assertEquals(expectedPost, userManager.updatePost(expectedPost));
    }

    @Test
    void deletePost_ShouldReturnTrueOnSuccess() {
        when(restTemplate.postForEntity(anyString(), any(Post.class), eq(Boolean.class)))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));
        
        assertTrue(userManager.deletePost(new Post()));
    }

    @Test
    void getAllUsers_ShouldReturnUsersList() {
        List<User> expectedUsers = Arrays.asList(new User(), new User());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), 
            any(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity<>(expectedUsers, HttpStatus.OK));
        
        assertEquals(expectedUsers, userManager.getAllUsers(TEST_TOKEN));
    }

    // Tests para casos de error en métodos de servicio

    @Test
    void getPosts_Unauthorized_ReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertNull(userManager.getPosts("invalidToken"));
    }

    @Test
    void getPostsOwner_Unauthorized_ReturnsNull() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), 
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertNull(userManager.getPostsOwner("invalidToken"));
    }

    @Test
    void getAllUsers_Unauthorized_ReturnsNull() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), 
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertNull(userManager.getAllUsers("invalidToken"));
    }

    @Test
    void createPost_Unauthorized_ReturnsNull() {
        when(restTemplate.postForEntity(anyString(), any(PostDTO.class), eq(Post.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertNull(userManager.createPost(new PostDTO("content", "invalidToken")));
    }

    @Test
    void updatePost_Unauthorized_ReturnsNull() {
        when(restTemplate.postForEntity(anyString(), any(Post.class), eq(Post.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertNull(userManager.updatePost(new Post()));
    }

    @Test
    void deletePost_Unauthorized_ReturnsFalse() {
        when(restTemplate.postForEntity(anyString(), any(Post.class), eq(Boolean.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertFalse(userManager.deletePost(new Post()));
    }

    // Tests para excepciones no manejadas específicamente

    @Test
    void register_OtherHttpError_ShouldThrow() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(User.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> 
            userManager.register(new User("user", "pass", "name", "surname", 25)));
    }

    @Test
    void login_OtherHttpError_ShouldThrow() {
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> 
            userManager.login(new User("user", "pass")));
    }

    @Test
    void getPosts_OtherError_ShouldThrow() {
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenThrow(new RuntimeException("Connection error"));

        assertThrows(RuntimeException.class, () -> userManager.getPosts(TEST_TOKEN));
    }
    
    @Test
    void register_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(User.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.register(new User("user", "pass", "name", "surname", 25)));
        
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }

    @Test
    void login_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.FORBIDDEN);
        when(restTemplate.postForEntity(anyString(), any(User.class), eq(String.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.login(new User("user", "pass")));
        
        assertEquals(HttpStatus.FORBIDDEN, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }

    @Test
    void logout_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
        when(restTemplate.postForEntity(anyString(), eq(TEST_TOKEN), eq(Boolean.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.logout(TEST_TOKEN));
        
        assertEquals(HttpStatus.BAD_GATEWAY, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }

    @Test
    void getPosts_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.NOT_FOUND);
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.getPosts(TEST_TOKEN));
        
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }

    @Test
    void getPostsOwner_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.FORBIDDEN);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                isNull(), 
                any(ParameterizedTypeReference.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.getPostsOwner(TEST_TOKEN));
        
        assertEquals(HttpStatus.FORBIDDEN, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }

    @Test
    void createPost_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(anyString(), any(PostDTO.class), eq(Post.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.createPost(new PostDTO("content", TEST_TOKEN)));
        
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }

    @Test
    void updatePost_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(Post.class), eq(Post.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.updatePost(new Post()));
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }

    @Test
    void deletePost_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE);
        when(restTemplate.postForEntity(anyString(), any(Post.class), eq(Boolean.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.deletePost(new Post()));
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }

    @Test
    void getAllUsers_OtherHttpError_ShouldThrowOriginalException() {
        HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.FORBIDDEN);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                isNull(), 
                any(ParameterizedTypeReference.class)))
            .thenThrow(expectedException);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, 
            () -> userManager.getAllUsers(TEST_TOKEN));
        
        assertEquals(HttpStatus.FORBIDDEN, thrown.getStatusCode());
        assertSame(expectedException, thrown);
    }
}
