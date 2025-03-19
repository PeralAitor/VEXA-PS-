package com.example.restapi.controller;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.User;
import com.example.restapi.service.AuthService;



@RestController
@RequestMapping("/auth")
public class AuthController {
	
	private final AuthService authService;
	private HashMap<User, String> activeTokens = new HashMap<>();
	
	public AuthController(AuthService authService) {
		this.authService = authService;
	}
	
	@PostMapping("/registration")
	public ResponseEntity<User> register(@RequestBody UserDTO userDTO) {
		System.out.println("\n\n UserDTO: \n\n" + userDTO.getUsername());
		if (authService.register(userDTO)) {
			return new ResponseEntity<>(HttpStatus.OK);
        }
		return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
}


