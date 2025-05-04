package com.example.restapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.restapi.dto.CredentialsDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.User;
import com.example.restapi.repository.UserRepository;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private static Map<String, User> tokenMap = new HashMap<>();

	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public boolean register(UserDTO userDTO) {
		
		String requestedUsername = userDTO.getUsername().trim().toLowerCase();
	    if ("admin".equals(requestedUsername)) {
	        return false;
	    }
		
		if (userRepository.findByUsername(userDTO.getUsername()) == null) {
			User user = new User(userDTO);
			userRepository.save(user);
			return true;
		}
		return false;
	}
	
	public String login(CredentialsDTO credentials) {
	    // Primero verificar admin (sin consultar DB)
	    if ("admin".equals(credentials.getUsername()) && "admin".equals(credentials.getPassword())) {
	        
	        // Verificar si ya está autenticado
	        boolean isAdminLoggedIn = tokenMap.values().stream()
	            .anyMatch(u -> "admin".equals(u.getUsername()));
	        
	        if (isAdminLoggedIn) {
	            return null;
	        }

	        // Crear usuario admin con rol
	        User adminUser = new User("admin", "admin");
	        
	        String token = "admin"; // Token aleatorio seguro
	        tokenMap.put(token, adminUser);
	        return token;
	    }

	    // Lógica para usuarios normales
	    User user = userRepository.findByUsername(credentials.getUsername());
	    
	    if (user != null && user.getPassword().equals(credentials.getPassword())) {
	        if (tokenMap.containsValue(user)) {
	            return null;
	        }
	        String token = generateToken();
	        tokenMap.put(token, user);
	        return token;
	    }
	    
	    return null;
	}

	public boolean logout(String token) {
		if (tokenMap.containsKey(token)) {
			tokenMap.remove(token);
			return true;
		}
		return false;
	}

	public static User getUserFromMap(String token) {
		User user = tokenMap.get(token);
		return user;
	}
	
	public List<User> getAllUsers(String token) {
		if (tokenMap.get(token) == null) {
			return null;
		}
		return userRepository.findAll();
	}
	
	public static synchronized String generateToken() {
        return Long.toHexString(System.currentTimeMillis());
    }
}