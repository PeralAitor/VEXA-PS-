package com.example.restapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.restapi.dto.CredentialsDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.User;
import com.example.restapi.repository.UserRepository;

/**
 * Servicio de autenticación y gestión de usuarios.
 * Proporciona métodos para registrar, autenticar y gestionar sesiones de usuarios.
 */
@Service
public class AuthService {

	/**
	 * Repositorio de usuarios para operaciones de base de datos.
	 */
	private final UserRepository userRepository;
	
	/**
	 * Mapa para almacenar tokens de sesión y usuarios asociados.
	 * Se utiliza para gestionar la autenticación y autorización de usuarios.
	 */
	private static Map<String, User> tokenMap = new HashMap<>();
	
	 /**
     * Constructor para inyección de dependencias.
     * @param userRepository Repositorio de usuarios para operaciones de base de datos
     */
	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	 /**
     * Registra un nuevo usuario en el sistema.
     * @param userDTO Datos del usuario a registrar
     * @return true si el registro fue exitoso, false si el usuario ya existe o intenta registrar "admin"
     */
	public boolean register(UserDTO userDTO) {
		
		String requestedUsername = userDTO.getUsername().trim().toLowerCase();
	    if ("admin".equals(requestedUsername)) {
	        return false;
	    }
		
		if (userRepository.findByUsername(userDTO.getUsername()) == null) {
			User user = new User(userDTO);
			userRepository.save(user);
			return true;
		}
		return false;
	}
	
	/**
     * Autentica un usuario y genera un token de sesión.
     * Casos especiales:
     * - Usuario "admin" con contraseña "admin" (no almacenado en BD)
     * - Impide múltiples sesiones para el mismo usuario
     * 
     * @param credentials Credenciales de autenticación
     * @return Token de sesión o null si falla la autenticación
     */
	public String login(CredentialsDTO credentials) {
	    // Primero verificar admin (sin consultar DB)
	    if ("admin".equals(credentials.getUsername()) && "admin".equals(credentials.getPassword())) {
	        
	        // Verificar si ya está autenticado
	        boolean isAdminLoggedIn = tokenMap.values().stream()
	            .anyMatch(u -> "admin".equals(u.getUsername()));
	        
	        if (isAdminLoggedIn) {
	            return null;
	        }

	        // Crear usuario admin con rol
	        User adminUser = new User("admin", "admin");
	        
	        String token = "admin"; // Token aleatorio seguro
	        tokenMap.put(token, adminUser);
	        return token;
	    }

	    // Lógica para usuarios normales
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

	/**
	 * Cierra la sesión de un usuario eliminando su token.
	 * @param token Token de sesión del usuario
	 * @return true si se cierra la sesión exitosamente, false si el token no es válido
	 */
	public boolean logout(String token) {
		if (tokenMap.containsKey(token)) {
			tokenMap.remove(token);
			return true;
		}
		return false;
	}

	/**
	 * Verifica si un token es válido.
	 * @param token Token de sesión
	 * @return true si el token es válido, false en caso contrario
	 */
	public static User getUserFromMap(String token) {
		User user = tokenMap.get(token);
		return user;
	}
	
	/**
	 * Verifica si un token es válido.
	 * @param token Token de sesión
	 * @return true si el token es válido, false en caso contrario
	 */
	public List<User> getAllUsers(String token) {
		if (tokenMap.get(token) == null) {
			return null;
		}
		return userRepository.findAll();
	}
	
	/**
	 * Verifica si un token es válido.
	 * @param token Token de sesión
	 * @return true si el token es válido, false en caso contrario
	 */
	public static synchronized String generateToken() {
        return Long.toHexString(System.currentTimeMillis());
    }
}