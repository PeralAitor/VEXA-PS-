package com.example.restapi.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.service.AuthService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
@Controller
public class UserManager {
	
	/**
     * @brief Constructor que inicializa el cliente REST y las URLs base
     * @details Establece las URLs de los controladores de autenticación y posts,
     *          y crea una instancia de RestTemplate para las comunicaciones HTTP
     */
	private final String USER_CONTROLLER_URL;
	private final String POST_CONTROLLER_URL;
	private final RestTemplate restTemplate;
	public String token;

	//	private String responseRegister;

	public UserManager() {
		USER_CONTROLLER_URL = "http://localhost:8080/auth";
		POST_CONTROLLER_URL = "http://localhost:8080/vexa";
		this.restTemplate = new RestTemplate();
	}
	
    /**
     * @brief Añade atributos comunes a todas las vistas
     * @param model Objeto Model para pasar atributos a la vista
     * @param request Objeto HttpServletRequest para obtener la URL actual
     * @note Añade al modelo:
     *       - currentUrl: URL actual de la solicitud
     *       - token: Token de autenticación del usuario
     */
	@ModelAttribute
	public void addAttributes(Model model, HttpServletRequest request) {
		model.addAttribute("currentUrl", ServletUriComponentsBuilder.fromRequestUri(request).toUriString());
		model.addAttribute("token", token);
	}
	
    /**
     * @brief Muestra el formulario de registro completo
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Nombre de la vista a renderizar ("registration")
     */
	@GetMapping("/")
	public String registerForm(User user) {
		return "index"; 
	}
	
    /**
     * @brief Muestra el formulario de registro completo
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Nombre de la vista a renderizar ("registration")
     */
	@GetMapping("/registration")
	public String showRegistrationForm(Model model) {
		return "registration";
	}

	/**
	 * @brief Procesa el formulario de registro de usuarios.
	 * 
	 * Crea un nuevo usuario con los parámetros recibidos y trata de registrarlo.
	 * Añade al modelo un mensaje de éxito o error según el resultado del registro.
	 * 
	 * @param username Nombre de usuario único (para login)
	 * @param password Contraseña del usuario
	 * @param name Nombre real del usuario
	 * @param surnames Apellidos del usuario
	 * @param age Edad del usuario en años
	 * @param model Objeto Model de Spring para pasar atributos a la vista
	 * @return String Nombre de la vista a renderizar ("registration")
	 * 
	 * @note Añade al modelo:
	 *       - successMessage si el registro fue exitoso
	 *       - errorMessage si el usuario ya existe
	 */
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
	
    /**
     * @brief Muestra el formulario de login
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Nombre de la vista a renderizar ("login")
     */
	@GetMapping("/login")
	public String showLoginForm(Model model) {
		return "login";
	}
	
    /**
     * @brief Procesa el formulario de login
     * @param username Nombre de usuario
     * @param password Contraseña del usuario
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Vista a renderizar según el tipo de usuario:
     *         - "admin" para administradores
     *         - "posts" para usuarios normales
     *         - "login" si falla la autenticación
     * @note Añade al modelo:
     *       - token: Token de autenticación
     *       - successMessage/errorMessage: Mensajes de estado
     *       - posts: Lista de posts (para usuarios)
     *       - users: Lista de usuarios (solo admin)
     */
	@PostMapping("/login")
	public String login(@RequestParam String username, @RequestParam String password, Model model) {
	    User user = new User(username, password);
	    
	    token = login(user);
	    
	    if (token != null) {
	    	if (token.equals("admin")) {
				model.addAttribute("token", token);
				model.addAttribute("successMessage", "Admin logged in successfully");
				
				List<Post> posts = getPosts(token);
		        model.addAttribute("posts", posts);
		        
		        List<User> users = getAllUsers(token);
		        model.addAttribute("users", users);
		        
				return "admin";
	    	} else {
		    	model.addAttribute("token", token);
		    	model.addAttribute("user", user);
		        model.addAttribute("successMessage", "User logged in successfully");
		        
		        List<Post> posts = getPosts(token);
		        model.addAttribute("posts", posts);
		        
		        return "posts";
	    	}
	    } else {
	        model.addAttribute("errorMessage", "User not found");
	        return "login";
	    }
	}
	
    /**
     * @brief Procesa el logout del usuario
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Nombre de la vista a renderizar ("index")
     * @note Añade al modelo:
     *       - successMessage/errorMessage: Mensajes de estado
     */
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
	
    /**
     * @brief Obtiene todos los posts visibles para el usuario
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Nombre de la vista a renderizar ("posts")
     * @note Añade al modelo:
     *       - posts: Lista de posts
     *       - user: Datos del usuario actual
     */
	@GetMapping("/posts")
	public String getPosts(Model model) {
		if (token != null) {
			List<Post> posts = getPosts(token);
	        model.addAttribute("posts", posts);
	        model.addAttribute("user", AuthService.getUserFromMap(token));
		}
	    return "posts";
	}
	
    /**
     * @brief Obtiene los posts del usuario actual
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Nombre de la vista a renderizar ("postsUser")
     * @note Añade al modelo:
     *       - posts: Lista de posts del usuario
     *       - user: Datos del usuario actual
     */
	@GetMapping("/posts/owner")
	public String getPostsOwner(Model model) {
		if (token != null) {
			List<Post> posts = getPostsOwner(token);
	        model.addAttribute("posts", posts);
	        model.addAttribute("user", AuthService.getUserFromMap(token));
		}
	    return "postsUser";
	}
	
    /**
     * @brief Muestra el formulario de creación de posts
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Nombre de la vista a renderizar ("post")
     */
	@GetMapping("/post") 
	public String post(Model model) {
		return "post";
	}
	
    /**
     * @brief Crea un nuevo post
     * @param content Contenido del post
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Vista a renderizar:
     *         - "post" con mensaje de éxito/error
     *         - "index" si no hay token válido
     * @note Añade al modelo:
     *       - successMessage/errorMessage: Mensajes de estado
     */
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
	
	@GetMapping("/post/update")
	public String updatePost(@RequestParam Long id, Model model) {
	    if (token != null) {
	        List<Post> posts = getPostsOwner(token);
	        Post post = posts.stream()
	        		.filter(p -> p.getId() == id)
	        	    .findFirst()
	        	    .orElse(null);
	        if (post != null) {
	        	System.out.println("NO ES NULL: " + post.getId() + " " + post.getContent() + " " );
	            model.addAttribute("post", post);
	            model.addAttribute("id", post.getId());
	            return "editPost";
	        }
	    }
	    return "index";
	}


	@PostMapping("/update/post")
	public String updatePost(@RequestParam Long id, @RequestParam String content, Model model) {
	    if (token != null) {
	    	
	    	List<Post> posts = getPostsOwner(token);
	    	Post post = posts.stream()
	    		    .filter(p -> p.getId() == id)
	    		    .findFirst()
	    		    .orElse(null);
	    	System.out.println("POST: " + post.getId() + " " + post.getContent() + " " );
	        if (post != null) {
	            post.setContent(content);
	            updatePost(post);
	            posts = getPostsOwner(token);
		        model.addAttribute("posts", posts);
		        model.addAttribute("user", AuthService.getUserFromMap(token));
	            return "postsUser";
	        }
	    }
	    return "index";
	}
	
	@PostMapping("/post/delete")
	public String deletePost(@RequestParam Long id, Model model) {
	    if (token != null) {
	    	if(token.equals("admin")) {
	    		
	    		Post post = new Post();
	 	        post.setId(id);
	 	        deletePost(post);
	    		
	    		model.addAttribute("token", token);
				model.addAttribute("successMessage", "Admin logged in successfully");
				
				List<Post> posts = getPosts(token);
		        model.addAttribute("posts", posts);
		        
		        List<User> users = getAllUsers(token);
		        model.addAttribute("users", users);
	    		
	        	return "admin";
	        }
	        Post post = new Post();
	        post.setId(id);
	        deletePost(post);
	        List<Post> posts = getPostsOwner(token);
	        model.addAttribute("posts", posts);
	        model.addAttribute("user", AuthService.getUserFromMap(token));
	        
	        
	        return "postsUser";
	    }
	    return "index";
	}
	
    /**
     * @brief Elimina un usuario (solo admin)
     * @param username Nombre de usuario a eliminar
     * @param model Objeto Model para pasar atributos a la vista
     * @return String Vista a renderizar:
     *         - "admin" con lista actualizada
     *         - "index" si no hay permisos
     * @note Añade al modelo:
     *       - posts: Lista actualizada de posts
     *       - users: Lista actualizada de usuarios
     *       - successMessage: Mensaje de confirmación
     */
	@PostMapping("/user/delete")
	public String deleteUser(@RequestParam String username, Model model) {
		if (token.equals("admin")) {
	        // Primero obtener todos los posts del usuario para forzar la carga de likes
	        List<Post> allPosts = getPosts(token);
	        
	        User user = new User();
	        user.setUsername(username);
	        deleteUser(user);
	        
	        // Actualizar la lista de posts después de eliminar los likes
	        List<Post> updatedPosts = getPosts(token);
	        model.addAttribute("posts", updatedPosts);
	        
	        List<User> users = getAllUsers(token);
	        model.addAttribute("users", users);
	        model.addAttribute("successMessage", "Usuario y sus likes eliminados correctamente");
	        return "admin";
	    }
	    return "index";
	}
	
	@PostMapping("/post/like")
	public String likePost(@RequestParam Long postId, Model model) {
	    if (token != null) {
	        Post post = likePost(postId, token);
	        List<Post> posts = getPosts(token);
	        model.addAttribute("posts", posts);
	        model.addAttribute("user", AuthService.getUserFromMap(token));
	        return "posts"; // Redirige a la vista de posts
	    }
	    return "index";
	}

	@PostMapping("/post/unlike")
	public String unlikePost(@RequestParam Long postId, Model model) {
	    if (token != null) {
	        Post post = unlikePost(postId, token);
	        List<Post> posts = getPosts(token);
	        model.addAttribute("posts", posts);
	        model.addAttribute("user", AuthService.getUserFromMap(token));
	        return "posts";
	    }
	    return "index";
	}

    /**
     * @brief Registra un nuevo usuario en el sistema
     * @param user Objeto User con los datos del usuario
     * @return boolean Resultado de la operación:
     *         - true: Registro exitoso
     *         - false: Usuario ya existe
     * @throws HttpClientErrorException Si ocurre un error HTTP inesperado
     */
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
	
    /**
     * @brief Autentica un usuario en el sistema
     * @param user Objeto User con credenciales
     * @return String Token de autenticación o null si falla
     * @throws HttpClientErrorException Si ocurre un error HTTP inesperado
     */
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

	        ResponseEntity<List<Post>> response = restTemplate.exchange(
	            url,
	            HttpMethod.GET,
	            null,
	            new ParameterizedTypeReference<List<Post>>() {}
	        );

	        return response.getBody();

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
	
	public Post updatePost(Post postDTO) {
		try {
			String url = POST_CONTROLLER_URL.concat("/post/update");
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
	
	public boolean deletePost(Post postDTO) {
	    try {
	        String url = POST_CONTROLLER_URL.concat("/post/delete");
	        ResponseEntity<Boolean> response = restTemplate.postForEntity(url, postDTO, Boolean.class);
	        return response.getBody();
	    } catch (HttpClientErrorException ex) {
	        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
	            return false;
	        } else {
	            throw ex;
	        }
	    }
	}
	
	public List<User> getAllUsers(String token) {
		try {
			String url = USER_CONTROLLER_URL.concat("/users?token=").concat(token);
			ResponseEntity<List<User>> response = restTemplate.exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<User>>() {
					});
			return response.getBody();
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				return null;
			} else {
				throw ex;
			}
		}
	}
	
	public void deleteUser(User user) {
	    try {
	        String url = POST_CONTROLLER_URL.concat("/user/delete");
	        restTemplate.postForEntity(url, user, Void.class);
	    } catch (HttpClientErrorException ex) {
	        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
	        } else {
	            throw ex;
	        }
	    }
	}
	
	/**
     * @brief Da like a un post específico
     * @param postId ID del post a likear
     * @param token Token de autenticación
     * @return Post Objeto Post actualizado
     */
	public Post likePost(Long postId, String token) {
	    String url = POST_CONTROLLER_URL.concat("/post/like?postId=" + postId + "&token=" + token);
	    return restTemplate.postForEntity(url, null, Post.class).getBody();
	}
	
	/**
     * @brief Quita like a un post específico
     * @param postId ID del post para quitar like
     * @param token Token de autenticación
     * @return Post Objeto Post actualizado
     */
	public Post unlikePost(Long postId, String token) {
	    String url = POST_CONTROLLER_URL.concat("/post/unlike?postId=" + postId + "&token=" + token);
	    return restTemplate.postForEntity(url, null, Post.class).getBody();
	}
}
