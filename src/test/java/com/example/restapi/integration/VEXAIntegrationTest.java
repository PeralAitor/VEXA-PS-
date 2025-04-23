package com.example.restapi.integration;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.List;
import com.example.restapi.dto.*;
import com.example.restapi.model.Post;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private String authToken;
    private final String TEST_USERNAME = "testuser_" + System.currentTimeMillis();
    private Post createdPost;

    @BeforeEach
    void setUp() {
        cleanTestData();
        
        // Registrar usuario
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
        assertTrue(registrationResponse.getStatusCode().is2xxSuccessful());

        // Autenticar
        CredentialsDTO credentials = new CredentialsDTO(TEST_USERNAME, "password");
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            "/auth/login", 
            credentials, 
            String.class
        );
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        authToken = loginResponse.getBody();
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        // Eliminar posts
        if (createdPost != null) {
            postRepository.delete(createdPost);
        }
        
        // Eliminar usuario
        userRepository.findById(TEST_USERNAME).ifPresent(user -> {
            userRepository.delete(user);
            userRepository.flush();
        });
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
        updateRequest.setContent("Updated Content");
        updateRequest.setOwner(TEST_USERNAME);
        updateRequest.setDate(new Date());

        ResponseEntity<Post> updateResponse = restTemplate.postForEntity(
            "/vexa/post/update",
            updateRequest,
            Post.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Updated Content", updateResponse.getBody().getContent());

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
        
        createdPost = null; // Reset para limpieza
    }

    @Test
    void testUnauthorizedPostOperations() {
        // Crear post con token inválido
        PostDTO invalidPost = new PostDTO("Contenido no autorizado", "token-invalido");
        ResponseEntity<Post> response = restTemplate.postForEntity(
            "/vexa/post",
            invalidPost,
            Post.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), 
                   "Acceso no autorizado permitido");
    }

    @Test
    void testUserRegistrationConflict() {
        // Intentar registrar usuario duplicado
        UserDTO duplicateUser = new UserDTO(
            TEST_USERNAME,
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
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(), 
                   "No se detectó conflicto de usuario");
    }

    @Test
    void testInvalidLogin() {
        // Credenciales incorrectas
        CredentialsDTO invalidCreds = new CredentialsDTO("usuario_inexistente", "wrongpass");
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/auth/login", 
            invalidCreds, 
            String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), 
                   "Credenciales inválidas aceptadas");
    }

    @Test
    void testLogoutFlow() {
        // Logout válido
        ResponseEntity<Boolean> logoutResponse = restTemplate.postForEntity(
            "/auth/logout",
            authToken,
            Boolean.class
        );
        assertTrue(logoutResponse.getBody(), "Logout fallido");

        // Intentar operación con token invalidado
        PostDTO post = new PostDTO("Post después de logout", authToken);
        ResponseEntity<Post> response = restTemplate.postForEntity(
            "/vexa/post",
            post,
            Post.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), 
                   "Token inválido aceptado");
    }
}