package com.example.restapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.CredentialsDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.User;
import com.example.restapi.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}
	/**
	 * * Endpoint para registrar un nuevo usuario.
	 * @param userDTO Claves para el registro de un usuario, incluye username, password, edad,etc...
	 * @return
	 */
	@PostMapping("/registration")
	public ResponseEntity<User> register(@RequestBody UserDTO userDTO) {
		if (authService.register(userDTO)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	/**
	 * Endpoint para iniciar sesión.
	 * 
	 * @param credentialsDTO Claves para el login de un usuario, incluye username y
	 *                       password
	 * @return
	 */
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody CredentialsDTO credentialsDTO) {
		String response = authService.login(credentialsDTO);
		if(response != null) {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Endpoint para cerrar sesión.
	 * 
	 * @param token Clave de sesión del usuario
	 * @return
	 */
	@PostMapping("/logout")
	public ResponseEntity<Boolean> logout(@RequestBody String token) {
		if(authService.logout(token)) {
			return new ResponseEntity<>(true, HttpStatus.OK);
		}
		return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Endpoint para obtener todos los usuarios.
	 * 
	 * @param token Clave de sesión del usuario
	 * @return
	 */
	@GetMapping("/users")
	public ResponseEntity<List<User>> getAllUsers(@RequestParam String token) {
		// Verificar si es admin
		User user = AuthService.getUserFromMap(token);
		if (user != null && "admin".equals(user.getUsername())) {
			return ResponseEntity.ok(authService.getAllUsers(token));
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}
}


