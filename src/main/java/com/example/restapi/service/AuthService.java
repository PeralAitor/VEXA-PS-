package com.example.restapi.service;

import java.util.HashMap;
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
		if (userRepository.findByUsername(userDTO.getUsername()) == null) {
			User user = new User(userDTO);
			userRepository.save(user);
			return true;
		}
		return false;
	}
	
	public String login(CredentialsDTO credentials) {
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
		System.out.println("Token: " + token + ", User: " + user);
		return user;
	}
	
	private static synchronized String generateToken() {
        return Long.toHexString(System.currentTimeMillis());
    }
}