package com.example.restapi.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.restapi.model.User;

import jakarta.servlet.http.HttpServletRequest;
@Controller
public class UserManager {

	private final String USER_CONTROLLER_URL;
	private final RestTemplate restTemplate;
	private String token;

	//	private String responseRegister;

	public UserManager() {
		USER_CONTROLLER_URL = "http://localhost:8080/auth";
		this.restTemplate = new RestTemplate();
	}

	@ModelAttribute
	public void addAttributes(Model model, HttpServletRequest request) {
		model.addAttribute("currentUrl", ServletUriComponentsBuilder.fromRequestUri(request).toUriString());
		model.addAttribute("token", token);
	}

	@GetMapping("/")
	public String registerForm(User user) {
		return "index"; 
	}

	@GetMapping("/registration")
	public String showRegistrationForm(Model model) {
		return "registration";
	}

	@PostMapping("/registration")
	public String registerUser(@RequestParam String username,@RequestParam String password,@RequestParam String name,
			@RequestParam String surnames,@RequestParam int age, Model model) {
				
		User user = new User(username, password, name, surnames, age);

		if (registerUser(user)) {
			model.addAttribute("successMessage", "User registered successfully");
		} else {
			model.addAttribute("errorMessage", "User already exists");
		}

		return "registration";
	}

	@GetMapping("/login")
	public String showLoginForm(Model model) {
		System.out.println("Debugging");
		return "login";
	}

	@PostMapping("/login")
	public String loginUser(@RequestParam String username, @RequestParam String password, Model model) {
		User user = new User(username, password);
		token = loginUser(user);
		if (token != null) {
			model.addAttribute("successMessage", "User logged in successfully");
		} else {
			model.addAttribute("errorMessage", "User not found");
		}
		return "login";
	}

	// A partir de aquí son las funciones que podríamos poner en el Service del lado del cliente
	public boolean registerUser(User user) {
		try {
			String url = USER_CONTROLLER_URL.concat("/registration");
			ResponseEntity<User> userResponse = restTemplate.postForEntity(url, user, User.class);
			return userResponse.getStatusCode().is2xxSuccessful();
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.CONFLICT) {
				return false;
			} else {
				throw ex;
			}
		}
	}
	
	public String loginUser(User user) {
		try {
			String url = USER_CONTROLLER_URL.concat("/login");
			ResponseEntity<String> userResponse = restTemplate.postForEntity(url, user, String.class);
			return userResponse.getBody();
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				return null;
			} else {
				throw ex;
			}
		}
	}




}
