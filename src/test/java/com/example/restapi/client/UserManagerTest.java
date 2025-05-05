package com.example.restapi.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;

class UserManagerTest {

    private UserManager userManager;
    private RestTemplate restTemplate;
    private Model model;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        restTemplate = mock(RestTemplate.class);
        model = mock(Model.class);
        ReflectionTestUtils.setField(userManager, "restTemplate", restTemplate);
    }

    // Tests para login(String, String, Model)

    @Test
    void testLogin_Success_Model() {
        User user = new User("username", "password");
        when(restTemplate.postForEntity(anyString(), eq(user), eq(String.class)))
            .thenReturn(new ResponseEntity<>("token", HttpStatus.OK));

        String view = userManager.login("username", "password", model);

        assertEquals("posts", view);
        verify(model).addAttribute(eq("token"), eq("token"));
        verify(model).addAttribute(eq("successMessage"), anyString());
    }

    @Test
    void testLogin_Failure_Model() {
        User user = new User("username", "password");
        when(restTemplate.postForEntity(anyString(), eq(user), eq(String.class)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        String view = userManager.login("username", "password", model);

        assertEquals("login", view);
        verify(model).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void testLogin_OtherException() {
        User user = new User("user", "pass");
        when(restTemplate.postForEntity(anyString(), eq(user), eq(String.class)))
            .thenThrow(new ResourceAccessException("Error"));

        assertThrows(ResourceAccessException.class, () -> userManager.login(user));
    }

    // Tests para logout(Model)

    @Test
    void testLogout_Success_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");
        when(restTemplate.postForEntity(anyString(), any(), eq(Boolean.class)))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        String view = userManager.logout(model);

        assertEquals("index", view);
        verify(model).addAttribute(eq("successMessage"), anyString());
    }

    @Test
    void testLogout_Failure_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");
        when(restTemplate.postForEntity(anyString(), any(), eq(Boolean.class)))
            .thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        String view = userManager.logout(model);

        assertEquals("index", view);
        verify(model).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void testLogout_OtherException() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Boolean.class)))
            .thenThrow(new ResourceAccessException("Error"));

        assertThrows(ResourceAccessException.class, () -> userManager.logout("token"));
    }

    
    // Tests para register(String, String, String, String, int, Model)

    @Test
    void testRegister_Success_Model() {
        User user = new User("username", "password", "name", "surname", 20);
        when(restTemplate.postForEntity(anyString(), eq(user), eq(User.class)))
            .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));

        String view = userManager.register("username", "password", "name", "surname", 20, model);

        assertEquals("registration", view);
        verify(model).addAttribute(eq("successMessage"), anyString());
    }

    @Test
    void testRegister_Failure_Model() {
        User user = new User("username", "password", "name", "surname", 20);
        when(restTemplate.postForEntity(anyString(), eq(user), eq(User.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

        String view = userManager.register("username", "password", "name", "surname", 20, model);

        assertEquals("registration", view);
        verify(model).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void testRegister_OtherException() {
        User user = new User("user", "pass", "name", "surname", 20);
        when(restTemplate.postForEntity(anyString(), eq(user), eq(User.class)))
            .thenThrow(new ResourceAccessException("Error"));

        assertThrows(ResourceAccessException.class, () -> userManager.register(user));
    }

    
    // Tests para createPost(String, Model)

    @Test
    void testCreatePost_Success_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");
        Post post = new Post();
        when(restTemplate.postForEntity(anyString(), any(), eq(Post.class)))
            .thenReturn(new ResponseEntity<>(post, HttpStatus.OK));

        String view = userManager.createPost("some content", model);

        assertEquals("post", view);
        verify(model).addAttribute(eq("successMessage"), anyString());
    }

    @Test
    void testCreatePost_Failure_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");
        when(restTemplate.postForEntity(anyString(), any(), eq(Post.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        String view = userManager.createPost("some content", model);

        assertEquals("post", view);
        verify(model).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void testCreatePost_NoToken() {
        ReflectionTestUtils.setField(userManager, "token", null);

        String view = userManager.createPost("some content", model);

        assertEquals("index", view);
    }
    
    @Test
    void testCreatePost_OtherException() {
        PostDTO postDTO = new PostDTO("content", "token");
        when(restTemplate.postForEntity(anyString(), eq(postDTO), eq(Post.class)))
            .thenThrow(new ResourceAccessException("Error"));

        assertThrows(ResourceAccessException.class, () -> userManager.createPost(postDTO));
    }


    // Tests para deletePost(Long, Model)

    @Test
    void testDeletePost_Success_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");
        when(restTemplate.postForEntity(anyString(), any(), eq(Boolean.class)))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));
        
        List<Post> posts = Collections.singletonList(new Post());
        ResponseEntity<List<Post>> response = new ResponseEntity<>(posts, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        String view = userManager.deletePost(1L, model);

        assertEquals("postsUser", view);
    }


    @Test
    void testDeletePost_NoToken() {
        ReflectionTestUtils.setField(userManager, "token", null);

        String view = userManager.deletePost(1L, model);

        assertEquals("index", view);
    }
    
    @Test
    void testDeletePost_OtherException() {
        Post post = new Post();
        when(restTemplate.postForEntity(anyString(), eq(post), eq(Boolean.class)))
            .thenThrow(new ResourceAccessException("Error"));

        assertThrows(ResourceAccessException.class, () -> userManager.deletePost(post));
    }

    @Test
    void testDeletePost_Success_ReturnsTrue() {
        Post post = new Post();
        when(restTemplate.postForEntity(anyString(), eq(post), eq(Boolean.class)))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));
        
        boolean result = userManager.deletePost(post);
        
        assertTrue(result);
    }

    @Test
    void testDeletePost_Unauthorized_ReturnsFalse() {
        Post post = new Post();
        when(restTemplate.postForEntity(anyString(), eq(post), eq(Boolean.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        boolean result = userManager.deletePost(post);
        
        assertFalse(result);
    }

    @Test
    void testDeletePost_OtherHttpClientError_ThrowsException() {
        Post post = new Post();
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(anyString(), eq(post), eq(Boolean.class)))
            .thenThrow(ex);
        
        assertThrows(HttpClientErrorException.class, () -> userManager.deletePost(post));
    }

    // Tests para updatePost(Long id, String content, Model model)

    @Test
    void testUpdatePost_Success_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");

        Post post = new Post();
        post.setId(1L);
        post.setContent("old content");

        List<Post> posts = Collections.singletonList(post);
        ResponseEntity<List<Post>> response = ResponseEntity.ok(posts);

        // Mockeamos getPostsOwner
        when(restTemplate.exchange(
                anyString(),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn((ResponseEntity) response);

        // Mockeamos updatePost
        when(restTemplate.postForEntity(anyString(), any(), eq(Post.class)))
            .thenReturn(new ResponseEntity<>(post, HttpStatus.OK));

        String view = userManager.updatePost(1L, "new content", model);

        assertEquals("postsUser", view);
    }


//    @Test
//    void testUpdatePost_PostNotFound_Model() {
//        ReflectionTestUtils.setField(userManager, "token", "validToken");
//
//        Post post = new Post(); // Post vacío, id == null
//
//        List<Post> posts = Collections.singletonList(post);
//        ResponseEntity<List<Post>> response = ResponseEntity.ok(posts);
//
//        when(restTemplate.exchange(
//                anyString(),
//                eq(org.springframework.http.HttpMethod.GET),
//                isNull(),
//                any(ParameterizedTypeReference.class)))
//            .thenReturn((ResponseEntity) response);
//
//        String view = userManager.updatePost(1L, "new content", model);
//
//        assertEquals("index", view);
//    }
    @Test
    void testGetPosts_Unauthorized_ReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        List<Post> result = userManager.getPosts("invalidToken");
        
        assertNull(result);
    }

    @Test
    void testGetPostsOwner_Unauthorized_ReturnsNull() {
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        List<Post> result = userManager.getPostsOwner("invalidToken");
        
        assertNull(result);
    }

    @Test
    void testGetPosts_OtherException_ThrowsException() {
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenThrow(new ResourceAccessException("Connection failed"));

        assertThrows(ResourceAccessException.class, () -> userManager.getPosts("token"));
    }

    @Test
    void testGetPostsOwner_OtherException_ThrowsException() {
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
            .thenThrow(new ResourceAccessException("Connection failed"));

        assertThrows(ResourceAccessException.class, () -> userManager.getPostsOwner("token"));
    }
    
    @Test
    void testRegister_Success_ReturnsTrue() {
        User user = new User("username", "password", "name", "surname", 20);
        when(restTemplate.postForEntity(anyString(), eq(user), eq(User.class)))
            .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));

        boolean result = userManager.register(user);
        
        assertTrue(result);
    }

    @Test
    void testRegister_Conflict_ReturnsFalse() {
        User user = new User("username", "password", "name", "surname", 20);
        when(restTemplate.postForEntity(anyString(), eq(user), eq(User.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

        boolean result = userManager.register(user);
        
        assertFalse(result);
    }

    @Test
    void testRegister_OtherException_ThrowsException() {
        User user = new User("username", "password", "name", "surname", 20);
        when(restTemplate.postForEntity(anyString(), eq(user), eq(User.class)))
            .thenThrow(new ResourceAccessException("Connection failed"));

        assertThrows(ResourceAccessException.class, () -> userManager.register(user));
    }

    @Test
    void testLogin_Success_ReturnsToken() {
        User user = new User("username", "password");
        when(restTemplate.postForEntity(anyString(), eq(user), eq(String.class)))
            .thenReturn(new ResponseEntity<>("token", HttpStatus.OK));

        String result = userManager.login(user);
        
        assertEquals("token", result);
    }
    
    @Test
    void testLogin_Unauthorized_ReturnsNull() {
        User user = new User("username", "password");
        when(restTemplate.postForEntity(anyString(), eq(user), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        String result = userManager.login(user);
        
        assertNull(result);
    }
    
    @Test
    void testLogin_HttpClientErrorExceptionOtherStatus_ThrowsException() {
        User user = new User("user", "pass");
        when(restTemplate.postForEntity(anyString(), eq(user), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> userManager.login(user));
    }

    @Test
    void testLogout_Success_ReturnsTrue() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Boolean.class)))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        boolean result = userManager.logout("validToken");
        
        assertTrue(result);
    }

    @Test
    void testLogout_Unauthorized_ReturnsFalse() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Boolean.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        boolean result = userManager.logout("invalidToken");
        
        assertFalse(result);
    }
    
    @Test
    void testLogout_HttpClientErrorExceptionOtherStatus_ThrowsException() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Boolean.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        assertThrows(HttpClientErrorException.class, () -> userManager.logout("token"));
    }
    
    @Test
    void testUpdatePost_OtherException() {
        Post post = new Post();
        when(restTemplate.postForEntity(anyString(), eq(post), eq(Post.class)))
            .thenThrow(new ResourceAccessException("Error"));

        assertThrows(ResourceAccessException.class, () -> userManager.updatePost(post));
    }


    @Test
    void testRegisterForm() {
        User user = new User();
        String view = userManager.registerForm(user);
        assertEquals("index", view);
    }

    @Test
    void testShowRegistrationForm() {
        String view = userManager.showRegistrationForm(model);
        assertEquals("registration", view);
    }
    
    @Test
    void testRegister_HttpClientErrorExceptionOtherStatus_ThrowsException() {
        User user = new User("username", "password", "name", "surname", 20);
        when(restTemplate.postForEntity(anyString(), eq(user), eq(User.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> userManager.register(user));
    }
    
    @Test
    void testshowLoginForm() {
        User user = new User();
        String view = userManager.registerForm(user);
        assertEquals("index", view);
    }
    
    @Test
    void testShowLoginForm() {
        String view = userManager.showLoginForm(model);
        assertEquals("login", view);
    }

    @Test
    void testPostForm() {
        String view = userManager.post(model);
        assertEquals("post", view);
    }

    // Tests para updatePost(Long id, Model model)

    @Test
    void testUpdatePostForm_Success_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");
        Post post = new Post();
        post.setId(1L);
        post.setContent("content");

        ResponseEntity<List> response = new ResponseEntity<>(Collections.singletonList(post), HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        String view = userManager.updatePost(1L, model);

        assertEquals("editPost", view);
        verify(model).addAttribute("post", post);
        verify(model).addAttribute("id", 1L);
    }

    @Test
    void testUpdatePostForm_NotFound_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");

        ResponseEntity<List> response = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        String view = userManager.updatePost(1L, model);

        assertEquals("index", view);
    }

    @Test
    void testUpdatePostForm_NoToken_Model() {
        ReflectionTestUtils.setField(userManager, "token", null);

        String view = userManager.updatePost(1L, model);

        assertEquals("index", view);
    }

    // --- Controller-level tests for updatePost (/update/post POST) ---

    @Test
    void testUpdatePost_PostUpdate_Success_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");
        
        // Prepare original post for find
        Post original = new Post();
        original.setId(1L);
        original.setContent("old content");
        ResponseEntity<List> findResponse = new ResponseEntity<>(Collections.singletonList(original), HttpStatus.OK);
        // Prepare updated post after update
        Post updated = new Post();
        updated.setId(1L);
        updated.setContent("new content");
        ResponseEntity<List> postUpdateFetch = new ResponseEntity<>(Collections.singletonList(updated), HttpStatus.OK);

        // Stub GET for owner posts: first call to find original, second to fetch updated list
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(findResponse, postUpdateFetch);

        // Stub updatePost service call
        when(restTemplate.postForEntity(
                contains("/post/update"),
                argThat(arg -> arg instanceof Post && ((Post)arg).getId() == 1L && "new content".equals(((Post)arg).getContent())),
                eq(Post.class)))
            .thenReturn(new ResponseEntity<>(updated, HttpStatus.OK));

        String view = userManager.updatePost(1L, "new content", model);

        assertEquals("postsUser", view);
        verify(model).addAttribute("posts", Collections.singletonList(updated));
        verify(model).addAttribute(eq("user"), any());
    }

    @Test
    void testUpdatePost_NoToken_ReturnsIndex() {
        ReflectionTestUtils.setField(userManager, "token", null);

        String view = userManager.updatePost(1L, "content", model);

        assertEquals("index", view);
    }
    
    @Test
    void testUpdatePost_Unauthorized_ReturnsNull() {
        Post post = new Post();
        when(restTemplate.postForEntity(anyString(), eq(post), eq(Post.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        Post result = userManager.updatePost(post);
        
        assertNull(result);
    }

    @Test
    void testUpdatePost_OtherHttpClientError_ThrowsException() {
        Post post = new Post();
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.FORBIDDEN);
        when(restTemplate.postForEntity(anyString(), eq(post), eq(Post.class)))
            .thenThrow(ex);
        
        assertThrows(HttpClientErrorException.class, () -> userManager.updatePost(post));
    }

    // Tests para getPostsOwner(Model)

    @Test
    void testGetPostsOwner_Success_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");

        ResponseEntity<List> response = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        String view = userManager.getPostsOwner(model);

        assertEquals("postsUser", view);
    }

    @Test
    void testGetPostsOwner_NoToken_Model() {
        ReflectionTestUtils.setField(userManager, "token", null);

        String view = userManager.getPostsOwner(model);

        assertEquals("postsUser", view);
    }
    
    @Test
    void testGetPostsOwner_OtherException() {
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
            .thenThrow(new ResourceAccessException("Error"));

        assertThrows(ResourceAccessException.class, () -> userManager.getPostsOwner("token"));
    }


    // Tests para getPosts(Model)

    @Test
    void testGetPosts_Success_Model() {
        ReflectionTestUtils.setField(userManager, "token", "validToken");

        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenReturn(Collections.emptyList());

        String view = userManager.getPosts(model);

        assertEquals("posts", view);
    }

    @Test
    void testGetPosts_NoToken_Model() {
        ReflectionTestUtils.setField(userManager, "token", null);

        String view = userManager.getPosts(model);

        assertEquals("posts", view);
    }

    @Test
    void testGetPosts_OtherException() {
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenThrow(new ResourceAccessException("Error"));

        assertThrows(ResourceAccessException.class, () -> userManager.getPosts("token"));
    }

    
    // Tests para addAttributes(Model, HttpServletRequest)

    @Test
    void testAddAttributes_Model() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");

        userManager.addAttributes(model, request);

        verify(model).addAttribute(eq("currentUrl"), anyString());
        verify(model).addAttribute(eq("token"), any());
    }
    @Test
    void testGetPosts_HttpClientErrorExceptionOtherStatus_ThrowsException() {
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        assertThrows(HttpClientErrorException.class, () -> userManager.getPosts("token"));
    }

    @Test
    void testGetPostsOwner_HttpClientErrorExceptionOtherStatus_ThrowsException() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        assertThrows(HttpClientErrorException.class, () -> userManager.getPostsOwner("token"));
    }
    
    @Test
    void testGetAllUsers_Success_ReturnsUserList() {
        List<User> expectedUsers = Collections.singletonList(new User());
        ResponseEntity<List<User>> response = new ResponseEntity<>(expectedUsers, HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        List<User> result = userManager.getAllUsers("validAdminToken");
        
        assertEquals(expectedUsers, result);
    }

    @Test
    void testGetAllUsers_Unauthorized_ReturnsNull() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        List<User> result = userManager.getAllUsers("invalidToken");
        
        assertNull(result);
    }

    @Test
    void testGetAllUsers_HttpClientErrorExceptionOtherStatus_ThrowsException() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        assertThrows(HttpClientErrorException.class, () -> userManager.getAllUsers("token"));
    }

    @Test
    void testGetAllUsers_OtherException_ThrowsException() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new ResourceAccessException("Connection failed"));

        assertThrows(ResourceAccessException.class, () -> userManager.getAllUsers("token"));
    }
    
    @Test
    void testLogin_Admin_Success_Model() {
        User user = new User("admin", "adminpass");
        when(restTemplate.postForEntity(anyString(), eq(user), eq(String.class)))
            .thenReturn(new ResponseEntity<>("admin", HttpStatus.OK));
        
        List<User> expectedUsers = Collections.singletonList(new User());
        ResponseEntity<List<User>> usersResponse = new ResponseEntity<>(expectedUsers, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("/users"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(usersResponse);
        
        List<Post> expectedPosts = Collections.singletonList(new Post());
        when(restTemplate.getForObject(anyString(), eq(List.class)))
            .thenReturn(expectedPosts);

        String view = userManager.login("admin", "adminpass", model);

        assertEquals("admin", view);
        verify(model).addAttribute("token", "admin");
        verify(model).addAttribute("successMessage", "Admin logged in successfully");
        verify(model).addAttribute("posts", expectedPosts);
        verify(model).addAttribute("users", expectedUsers);
    }
    
    @Test
    void testCreatePost_Success_ReturnsPost() {
        PostDTO postDTO = new PostDTO("content", "validToken");
        Post expectedPost = new Post();
        when(restTemplate.postForEntity(anyString(), eq(postDTO), eq(Post.class)))
            .thenReturn(new ResponseEntity<>(expectedPost, HttpStatus.OK));

        Post result = userManager.createPost(postDTO);
        
        assertEquals(expectedPost, result);
    }

    @Test
    void testCreatePost_Unauthorized_ReturnsNull() {
        PostDTO postDTO = new PostDTO("content", "invalidToken");
        when(restTemplate.postForEntity(anyString(), eq(postDTO), eq(Post.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        Post result = userManager.createPost(postDTO);
        
        assertNull(result);
    }

    @Test
    void testCreatePost_OtherHttpClientError_ThrowsException() {
        PostDTO postDTO = new PostDTO("content", "token");
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.FORBIDDEN);
        when(restTemplate.postForEntity(anyString(), eq(postDTO), eq(Post.class)))
            .thenThrow(ex);

        assertThrows(HttpClientErrorException.class, () -> userManager.createPost(postDTO));
    }

    @Test
    void testCreatePost_OtherException_ThrowsException() {
        PostDTO postDTO = new PostDTO("content", "token");
        when(restTemplate.postForEntity(anyString(), eq(postDTO), eq(Post.class)))
            .thenThrow(new ResourceAccessException("Connection failed"));

        assertThrows(ResourceAccessException.class, () -> userManager.createPost(postDTO));
    }
}