package com.example.restapi.controller;

import com.example.restapi.dto.CredentialsDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.User;
import com.example.restapi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

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
}
