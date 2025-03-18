package com.example.restapi.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.restapi.model.User;
import jakarta.servlet.http.HttpServletRequest;

public class UserManager {

	private String USER_CONTROLLER_URL_TEMPLATE = "http://%s:%s/auth/registration";
	private final String USER_CONTROLLER_URL;
	private final RestTemplate restTemplate;
	
//	private String responseRegister;
	
	public UserManager(String hostname, String port) {
		USER_CONTROLLER_URL = String.format(USER_CONTROLLER_URL_TEMPLATE, hostname, port);
		this.restTemplate = new RestTemplate();
	}
	
//	@ModelAttribute
//	public void addAttributes(Model model, HttpServletRequest request) {
//		model.addAttribute("currentUrl", ServletUriComponentsBuilder.fromRequestUri(request).toUriString());
//		model.addAttribute("responseRegister", responseRegister);
//	}
	
	@PostMapping("/auth/registration")
	public String registerUser(@RequestParam String username, @RequestParam String password, 
			@RequestParam String name, @RequestParam String surnames, @RequestParam int age, Model model) {
		
		User user = new User(username, password, name, surnames, age);
		
		if (registerUser(user)) {
			model.addAttribute("successMessage", "User registered successfully");
		} else {
			model.addAttribute("errorMessage", "User already exists");
		}
		
		return "registration";
	}
	
	public boolean registerUser(User user) {
		ResponseEntity<Void> userResponse = restTemplate.postForEntity(USER_CONTROLLER_URL, user, Void.class);
		
		if (userResponse.getStatusCode().is2xxSuccessful()) {
			return true;
		} else {
			return false;
		}
	}
	
}
