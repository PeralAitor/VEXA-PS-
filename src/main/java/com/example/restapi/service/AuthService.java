package com.example.restapi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
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
	
	private static synchronized String generateToken() {
        return Long.toHexString(System.currentTimeMillis());
    }
}