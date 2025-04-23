package com.example.restapi.integration;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.List;
import com.example.restapi.dto.*;
import com.example.restapi.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VexaIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;
    private Post createdPost;
    private final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Registrar y autenticar usuario
        UserDTO user = new UserDTO(
            TEST_USERNAME,
            "password",
            "Test User",
            "Test Surnames",
            25
        );
        
        ResponseEntity<Void> registrationResponse = restTemplate.postForEntity(
            "/auth/registration", 
            user, 
            Void.class
        );
        assertEquals(HttpStatus.OK, registrationResponse.getStatusCode());

        // Login para obtener token
        CredentialsDTO credentials = new CredentialsDTO(TEST_USERNAME, "password");
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            "/auth/login", 
            credentials, 
            String.class
        );
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        authToken = loginResponse.getBody();
    }

    @Test
    void testFullPostLifecycle() {
        // Crear post
        PostDTO postDTO = new PostDTO("Test Content", authToken);
        ResponseEntity<Post> createResponse = restTemplate.postForEntity(
            "/vexa/post", 
            postDTO, 
            Post.class
        );
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        createdPost = createResponse.getBody();
        assertNotNull(createdPost.getId());

        // Verificar creación
        ResponseEntity<List<Post>> postsResponse = restTemplate.exchange(
            "/vexa/posts?token=" + authToken,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Post>>() {}
        );
        assertTrue(postsResponse.getBody().stream()
            .anyMatch(p -> p.getId() == createdPost.getId()));

        // Actualizar post
        Post updateRequest = new Post();
        updateRequest.setId(createdPost.getId());
        updateRequest.setContent("Contenido actualizado");
        updateRequest.setOwner(TEST_USERNAME);
        updateRequest.setDate(new Date());

        ResponseEntity<Post> updateResponse = restTemplate.postForEntity(
            "/vexa/post/update",
            updateRequest,
            Post.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Contenido actualizado", updateResponse.getBody().getContent());

        // Eliminar post
        Post deleteRequest = new Post();
        deleteRequest.setId(createdPost.getId());
        deleteRequest.setOwner(TEST_USERNAME);

        ResponseEntity<Boolean> deleteResponse = restTemplate.postForEntity(
            "/vexa/post/delete",
            deleteRequest,
            Boolean.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertTrue(deleteResponse.getBody());

        // Verificar eliminación
        ResponseEntity<List<Post>> postsAfterDelete = restTemplate.exchange(
            "/vexa/posts?token=" + authToken,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Post>>() {}
        );
        assertFalse(postsAfterDelete.getBody().stream()
            .anyMatch(p -> p.getId() == createdPost.getId()));
    }

    @Test
    void testUnauthorizedPostOperations() {
        // Crear post con token inválido
        PostDTO invalidPost = new PostDTO("Contenido no autorizado", "token-invalido");
        ResponseEntity<Post> createResponse = restTemplate.postForEntity(
            "/vexa/post",
            invalidPost,
            Post.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, createResponse.getStatusCode());
    }

    @Test
    void testPostOwnershipValidation() {
        // Crear post válido
        PostDTO validPost = new PostDTO("Contenido válido", authToken);
        ResponseEntity<Post> createResponse = restTemplate.postForEntity(
            "/vexa/post",
            validPost,
            Post.class
        );
        Post createdPost = createResponse.getBody();

        // Intentar actualizar con usuario diferente
        Post invalidUpdate = new Post();
        invalidUpdate.setId(createdPost.getId());
        invalidUpdate.setContent("Hackeado");
        invalidUpdate.setOwner("otro_usuario");
        invalidUpdate.setDate(new Date());

        ResponseEntity<Post> updateResponse = restTemplate.postForEntity(
            "/vexa/post/update",
            invalidUpdate,
            Post.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, updateResponse.getStatusCode());
    }

    @Test
    void testUserRegistrationConflict() {
        UserDTO duplicateUser = new UserDTO(
            TEST_USERNAME, // Mismo username
            "nuevapass",
            "Usuario Duplicado",
            "Apellidos",
            30
        );
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/auth/registration",
            duplicateUser,
            Void.class
        );
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testLogoutFlow() {
        // Logout válido
        ResponseEntity<Boolean> logoutResponse = restTemplate.postForEntity(
            "/auth/logout",
            authToken,
            Boolean.class
        );
        assertTrue(logoutResponse.getBody());

        // Intentar operación post-logout
        PostDTO post = new PostDTO("Post después de logout", authToken);
        ResponseEntity<Post> createResponse = restTemplate.postForEntity(
            "/vexa/post",
            post,
            Post.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, createResponse.getStatusCode());
    }
}