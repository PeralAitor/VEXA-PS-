package com.example.restapi.service;

import com.example.restapi.dto.CredentialsDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.User;
import com.example.restapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private UserDTO userDTO;
    private User user;
    private CredentialsDTO credentials;

    @BeforeEach
    public void setUp() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        MockitoAnnotations.openMocks(this);

        userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");

        user = new User(userDTO);

        credentials = new CredentialsDTO();
        credentials.setUsername("testuser");
        credentials.setPassword("password");
        
        java.lang.reflect.Field tokenMapField = AuthService.class.getDeclaredField("tokenMap");
        tokenMapField.setAccessible(true);
        ((Map<?, ?>) tokenMapField.get(null)).clear();
    }

    @Test
    public void registerSuccessTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        boolean result = authService.register(userDTO);

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void registerFailureUserExistsTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        boolean result = authService.register(userDTO);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void loginSuccessTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        String token = authService.login(credentials);

        assertNotNull(token);
        assertEquals(user, AuthService.getUserFromMap(token));
    }

    @Test
    public void loginFailureInvalidCredentialsTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        credentials.setPassword("wrongPassword");
        String token = authService.login(credentials);

        assertNull(token);
    }

    @Test
    public void loginFailureUserAlreadyLoggedInTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        String token1 = authService.login(credentials);
        assertNotNull(token1);

        // Intenta loguear de nuevo al mismo usuario
        String token2 = authService.login(credentials);
        assertNull(token2);
    }

    @Test
    public void logoutSuccessTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        String token = authService.login(credentials);
        boolean result = authService.logout(token);

        assertTrue(result);
    }

    @Test
    public void logoutFailureInvalidTokenTest() {
        boolean result = authService.logout("invalidToken");

        assertFalse(result);
    }

    @Test
    public void getUserFromMapTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        String token = authService.login(credentials);
        User retrievedUser = AuthService.getUserFromMap(token);

        assertNotNull(retrievedUser);
        assertEquals("testuser", retrievedUser.getUsername());
    }
    
    @Test
    public void registerAdminUsernameShouldFail() {
        userDTO.setUsername("admin");
        boolean result = authService.register(userDTO);
        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void loginAdminSuccessTest() {
        credentials.setUsername("admin");
        credentials.setPassword("admin");
        
        String token = authService.login(credentials);
        
        assertEquals("admin", token);
        assertNotNull(AuthService.getUserFromMap(token));
    }

    @Test
    public void loginAdminAlreadyLoggedInTest() {
        credentials.setUsername("admin");
        credentials.setPassword("admin");
        
        // Primer login exitoso
        String firstToken = authService.login(credentials);
        assertNotNull(firstToken);
        
        // Segundo intento debería fallar
        String secondToken = authService.login(credentials);
        assertNull(secondToken);
    }

    @Test
    public void loginAdminWrongPasswordTest() {
        credentials.setUsername("admin");
        credentials.setPassword("wrongpassword");
        
        String token = authService.login(credentials);
        assertNull(token);
    }

    @Test
    public void getAllUsersWithValidTokenTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userRepository.findAll()).thenReturn(List.of(user));
        
        String token = authService.login(credentials);
        List<User> users = authService.getAllUsers(token);
        
        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    public void getAllUsersWithInvalidTokenTest() {
        List<User> users = authService.getAllUsers("invalidToken");
        assertNull(users);
    }

    @Test
    public void getUserFromMapWithInvalidTokenTest() {
        User user = AuthService.getUserFromMap("invalidToken");
        assertNull(user);
    }


    @Test
    public void loginUserNotFoundTest() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        
        String token = authService.login(credentials);
        assertNull(token);
    }
}
