package com.example.restapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restapi.dto.CredentialsDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.User;
import com.example.restapi.repository.UserRepository;

@Service
public class AuthService {

	private final UserRepository userRepository;

	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	@Transactional
	public boolean register(UserDTO userDTO) {
		System.out.println("UserDTO: " + userDTO.getUsername());
		if (userRepository.findByUsername(userDTO.getUsername()) == null) {
			User user = new User(userDTO);
			userRepository.save(user);
			return true;
		}
		return false;
	}
	
	public boolean login(CredentialsDTO credentials) {
		User user = userRepository.findByUsername(credentials.getUsername());
		if (user != null && user.getPassword().equals(credentials.getPassword())) {
			return true;
		}
		return false;
	}

}

