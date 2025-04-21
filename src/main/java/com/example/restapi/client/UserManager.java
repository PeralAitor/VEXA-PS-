package com.example.restapi.client;

import java.util.List;

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

import com.example.restapi.dto.PostDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;

import jakarta.servlet.http.HttpServletRequest;
@Controller
public class UserManager {

	private final String USER_CONTROLLER_URL;
	private final String POST_CONTROLLER_URL;
	private final RestTemplate restTemplate;
	private String token;

	//	private String responseRegister;

	public UserManager() {
		USER_CONTROLLER_URL = "http://localhost:8080/auth";
		POST_CONTROLLER_URL = "http://localhost:8080/vexa";
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
	public String register(@RequestParam String username,@RequestParam String password,@RequestParam String name,
			@RequestParam String surnames,@RequestParam int age, Model model) {
				
		User user = new User(username, password, name, surnames, age);

		if (register(user)) {
			model.addAttribute("successMessage", "User registered successfully");
		} else {
			model.addAttribute("errorMessage", "User already exists");
		}

		return "registration";
	}

	@GetMapping("/login")
	public String showLoginForm(Model model) {
		return "login";
	}

	@PostMapping("/login")
	public String login(@RequestParam String username, @RequestParam String password, Model model) {
	    User user = new User(username, password);
	    
	    token = login(user);
	    
	    if (token != null) {
	        model.addAttribute("token", token);
	        model.addAttribute("successMessage", "User logged in successfully");
	        
	        List<Post> posts = getPosts(token);
	        model.addAttribute("posts", posts);
	        
	        return "posts";
	    } else {
	        model.addAttribute("errorMessage", "User not found");
	        return "login";
	    }
	}
	
	@PostMapping("/logout")
	public String logout(Model model) {
		if (logout(token)) {
			token = null;
			model.addAttribute("token", token);
			model.addAttribute("successMessage", "User logged out successfully");
		} else {
			model.addAttribute("errorMessage", "User not found");
		}
		return "index";
	}
	
	@GetMapping("/posts")
	public String getPosts(Model model) {
		if (token != null) {
			List<Post> posts = getPosts(token);
	        model.addAttribute("posts", posts);	
		}
	    return "posts";
	}
	
	@GetMapping("/posts/owner")
	public String getPostsOwner(Model model) {
		if (token != null) {
			List<Post> posts = getPostsOwner(token);
	        model.addAttribute("posts", posts);	
		}
	    return "posts";
	}
	
	@GetMapping("/post") 
	public String post(Model model) {
		return "post";
	}
	
	@PostMapping("/post")
	public String createPost(@RequestParam String content, Model model) {
		if (token != null) {
			PostDTO postDTO = new PostDTO(content, token);
			Post post = createPost(postDTO);
			
			if (post != null) {
				model.addAttribute("successMessage", "Post created successfully");
			} else {
				model.addAttribute("errorMessage", "Post not created");
			}
			
			return "post";
		}
		return "index";
	}


	// A partir de aquí son las funciones que podríamos poner en el Service del lado del cliente
	public boolean register(User user) {
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
	
	public String login(User user) {
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
	
	public boolean logout(String token) {
		try {
			String url = USER_CONTROLLER_URL.concat("/logout");
			ResponseEntity<Boolean> userResponse = restTemplate.postForEntity(url, token, Boolean.class);
			return userResponse.getBody();
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				return false;
			} else {
				throw ex;
			}
		}
	}
	
	public List<Post> getPosts(String token) {
		try {
			String url = POST_CONTROLLER_URL.concat("/posts?token=").concat(token);
			
			List<Post> listPosts = restTemplate.getForObject(url, List.class);
			return listPosts;
			
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				return null;
			} else {
				throw ex;
			}
		}
	}
	
	public List<Post> getPostsOwner(String token) {
		try {
			String url = POST_CONTROLLER_URL.concat("/posts/user?token=").concat(token);
			
			List<Post> listPosts = restTemplate.getForObject(url, List.class);
			return listPosts;
			
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				return null;
			} else {
				throw ex;
			}
		}
	}
	
	public Post createPost(PostDTO postDTO) {
		try {
			String url = POST_CONTROLLER_URL.concat("/post");
			ResponseEntity<Post> response = restTemplate.postForEntity(url, postDTO, Post.class);
			return response.getBody();
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				return null;
			} else {
				throw ex;
			}
		}
	}
}
