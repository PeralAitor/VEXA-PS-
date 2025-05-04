package com.example.restapi.controller;

import com.example.restapi.dto.CredentialsDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.User;
import com.example.restapi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;
    
    private final String ADMIN_TOKEN = "admin-token";
    private final String USER_TOKEN = "user-token";
    private final String INVALID_TOKEN = "invalid-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void registerSuccessTest() {
        UserDTO userDTO = new UserDTO();
        when(authService.register(userDTO)).thenReturn(true);

        ResponseEntity<User> response = authController.register(userDTO);

        assertEquals(200, response.getStatusCodeValue());
        verify(authService, times(1)).register(userDTO);
    }

    @Test
    public void registerConflictTest() {
        UserDTO userDTO = new UserDTO();
        when(authService.register(userDTO)).thenReturn(false);

        ResponseEntity<User> response = authController.register(userDTO);

        assertEquals(409, response.getStatusCodeValue());
        verify(authService, times(1)).register(userDTO);
    }

    @Test
    public void loginSuccessTest() {
        CredentialsDTO credentialsDTO = new CredentialsDTO();
        when(authService.login(credentialsDTO)).thenReturn("valid_token");

        ResponseEntity<String> response = authController.login(credentialsDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("valid_token", response.getBody());
        verify(authService, times(1)).login(credentialsDTO);
    }

    @Test
    public void loginUnauthorizedTest() {
        CredentialsDTO credentialsDTO = new CredentialsDTO();
        when(authService.login(credentialsDTO)).thenReturn(null);

        ResponseEntity<String> response = authController.login(credentialsDTO);

        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(authService, times(1)).login(credentialsDTO);
    }

    @Test
    public void logoutSuccessTest() {
        String token = "valid_token";
        when(authService.logout(token)).thenReturn(true);

        ResponseEntity<Boolean> response = authController.logout(token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
        verify(authService, times(1)).logout(token);
    }

    @Test
    public void testLogoutUnauthorized() {
        String token = "invalid_token";
        when(authService.logout(token)).thenReturn(false);

        ResponseEntity<Boolean> response = authController.logout(token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody());
        verify(authService, times(1)).logout(token);
    }

    @Test
    void getAllUsers_AdminToken_ReturnsUsersList() {
        // Configurar usuario admin
        User adminUser = new User();
        adminUser.setUsername("admin");

        try (MockedStatic<AuthService> mocked = mockStatic(AuthService.class)) {
            mocked.when(() -> AuthService.getUserFromMap(ADMIN_TOKEN)).thenReturn(adminUser);
            
            // Configurar respuesta del servicio
            List<User> expectedUsers = List.of(new User(), new User());
            when(authService.getAllUsers(ADMIN_TOKEN)).thenReturn(expectedUsers);

            // Ejecutar
            ResponseEntity<List<User>> response = authController.getAllUsers(ADMIN_TOKEN);

            // Verificar
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedUsers, response.getBody());
            verify(authService).getAllUsers(ADMIN_TOKEN);
        }
    }

    @Test
    void getAllUsers_NonAdminToken_ReturnsForbidden() {
        // Configurar usuario normal
        User regularUser = new User();
        regularUser.setUsername("regular-user");

        try (MockedStatic<AuthService> mocked = mockStatic(AuthService.class)) {
            mocked.when(() -> AuthService.getUserFromMap(USER_TOKEN)).thenReturn(regularUser);

            // Ejecutar
            ResponseEntity<List<User>> response = authController.getAllUsers(USER_TOKEN);

            // Verificar
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());
            verify(authService, never()).getAllUsers(any());
        }
    }

    @Test
    void getAllUsers_InvalidToken_ReturnsForbidden() {
        try (MockedStatic<AuthService> mocked = mockStatic(AuthService.class)) {
            mocked.when(() -> AuthService.getUserFromMap(INVALID_TOKEN)).thenReturn(null);

            // Ejecutar
            ResponseEntity<List<User>> response = authController.getAllUsers(INVALID_TOKEN);

            // Verificar
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());
            verify(authService, never()).getAllUsers(any());
        }
    }

    @Test
    void getAllUsers_NullToken_ReturnsForbidden() {
        try (MockedStatic<AuthService> mocked = mockStatic(AuthService.class)) {
            mocked.when(() -> AuthService.getUserFromMap(null)).thenReturn(null);

            // Ejecutar
            ResponseEntity<List<User>> response = authController.getAllUsers(null);

            // Verificar
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());
            verify(authService, never()).getAllUsers(any());
        }
    }

    @Test
    void getAllUsers_AdminToken_ServiceReturnsEmptyList_ReturnsOkWithEmptyList() {
        // Configurar usuario admin
        User adminUser = new User();
        adminUser.setUsername("admin");

        try (MockedStatic<AuthService> mocked = mockStatic(AuthService.class)) {
            mocked.when(() -> AuthService.getUserFromMap(ADMIN_TOKEN)).thenReturn(adminUser);
            
            // Configurar respuesta vacía del servicio
            when(authService.getAllUsers(ADMIN_TOKEN)).thenReturn(Collections.emptyList());

            // Ejecutar
            ResponseEntity<List<User>> response = authController.getAllUsers(ADMIN_TOKEN);

            // Verificar
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
            verify(authService).getAllUsers(ADMIN_TOKEN);
        }
    }
}
