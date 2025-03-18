package com.example.restapi.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.restapi.model.User;
import jakarta.servlet.http.HttpServletRequest;
@Controller
public class UserManager {

	private String USER_CONTROLLER_URL_TEMPLATE = "http://%s:%s/auth/registration";
	private final String USER_CONTROLLER_URL;
	private final RestTemplate restTemplate;
	
//	private String responseRegister;
	
	public UserManager(String hostname, String port) {
		USER_CONTROLLER_URL = String.format(USER_CONTROLLER_URL_TEMPLATE, hostname, port);
		this.restTemplate = new RestTemplate();
	}

	public UserManager() {
        USER_CONTROLLER_URL = "http://localhost:8080/auth/registration";
        this.restTemplate = new RestTemplate();
	}
	
	@ModelAttribute
	public void addAttributes(Model model, HttpServletRequest request) {
		model.addAttribute("currentUrl", ServletUriComponentsBuilder.fromRequestUri(request).toUriString());
	}
	
	
	
	@PostMapping("/registration")
	public String registerUser(@RequestParam String username,@RequestParam String password,@RequestParam String name,
			@RequestParam String surnames,@RequestParam int age, Model model) {
		System.out.println("User: " + username + " " + password + " " + name + " " + surnames + " " + age);
		User user = new User(username, password, name, surnames, age);
		
		if (registerUser(user)) {
			model.addAttribute("successMessage", "User registered successfully");
		} else {
			model.addAttribute("errorMessage", "User already exists");
		}
		
		return "registration";
	}
	
	public boolean registerUser(User user) {
		System.out.println("User: " + user.getUsername() + " " + user.getPassword() + " " + user.getName() + " " + user.getSurnames() + " " + user.getAge());
		ResponseEntity<User> userResponse = restTemplate.postForEntity(USER_CONTROLLER_URL, user, User.class);
		System.out.println(userResponse.getStatusCode());
		if (userResponse.getStatusCode().is2xxSuccessful()) {
			return true;
		} else {
			return false;
		}
	}

	 @GetMapping("/home")
	    public String registerForm(User user) {
	        return "registration"; 
	    }
	
}
