package com.example.restapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.dto.TokenDTO;
import com.example.restapi.dto.UserDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.service.AuthService;
import com.example.restapi.service.VEXAService;


/**
 * Controlador REST para gestionar operaciones relacionadas con posts y usuarios VEXA.
 * Proporciona endpoints para creación, modificación y consulta de contenido.
 * 
 * @RestController Indica que esta clase es un controlador REST
 * @RequestMapping("/vexa") Define la ruta base para todos los endpoints en este controlador
 */
@RestController
@RequestMapping("/vexa")
public class VEXAController {

    private final VEXAService vexaService;
    private final PostRepository postRepository;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param vexaService Servicio principal de lógica de negocio
     * @param postRepository Repositorio para operaciones con posts
     */
    public VEXAController(VEXAService vexaService, PostRepository postRepository) {
        this.vexaService = vexaService;
        this.postRepository = postRepository;
    }

    /**
     * Crea un nuevo post en el sistema.
     *
     * @param postDTO DTO con los datos del post a crear
     * @return ResponseEntity con el post creado (200 OK) o UNAUTHORIZED (401) si falla la autenticación
     */
    @PostMapping("/post")
    public ResponseEntity<Post> createPost(@RequestBody PostDTO postDTO) {
        Post response = vexaService.createPost(postDTO, postDTO.getToken());
        if(response != null) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Obtiene todos los posts disponibles.
     *
     * @param token Token de autenticación del usuario
     * @return ResponseEntity con lista de posts (200 OK)
     */
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getPosts(@RequestParam String token) {
        List<Post> response = vexaService.getPosts(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Obtiene los posts creados por el usuario actual.
     *
     * @param token Token de autenticación del usuario
     * @return ResponseEntity con lista de posts del usuario (200 OK)
     */
    @GetMapping("/posts/user")
    public ResponseEntity<List<Post>> getPostsByUser(@RequestParam String token) {
        List<Post> response = vexaService.getPostsByUser(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Actualiza un post existente.
     *
     * @param postDTO Objeto Post con los datos actualizados
     * @return ResponseEntity con el post actualizado (200 OK) o UNAUTHORIZED (401) si falla la autenticación
     */
    @PostMapping("/post/update")
    public ResponseEntity<Post> updatePost(@RequestBody Post postDTO) {
        Post updatedPost = vexaService.updatePost(postDTO);
        if (updatedPost != null) {
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Elimina un post del sistema.
     *
     * @param postDTO Objeto Post a eliminar
     * @return ResponseEntity con true si fue exitoso (200 OK) o UNAUTHORIZED (401) si falla
     */
    @PostMapping("/post/delete")
    public ResponseEntity<Boolean> deletePost(@RequestBody Post postDTO) {
    	Boolean response = vexaService.deletePost(postDTO);
    	if(response == true) {
    		return new ResponseEntity<Boolean>(response, HttpStatus.OK);
    	}
    	return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param userDTO DTO con los datos del usuario a eliminar
     */
    @PostMapping("/user/delete")
    public void deleteUser(@RequestBody UserDTO userDTO) {
		vexaService.deleteUser(userDTO);
	}

    /**
     * Añade un "like" a un post específico.
     *
     * @param postId ID del post a likear
     * @param token Token de autenticación del usuario
     * @return ResponseEntity con el post actualizado (200 OK), NOT_FOUND (404) si no existe el post o UNAUTHORIZED (401) si token inválido
     */
    @PostMapping("/post/like")
    public ResponseEntity<Post> likePost(@RequestParam Long postId, @RequestParam String token) {
        User user = AuthService.getUserFromMap(token);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Post post = vexaService.likePost(postId, user.getUsername());
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();
    }

    /**
     * Remueve un "like" de un post específico.
     *
     * @param postId ID del post para remover like
     * @param token Token de autenticación del usuario
     * @return ResponseEntity con el post actualizado (200 OK), NOT_FOUND (404) si no existe el post o UNAUTHORIZED (401) si token inválido
     */
    @PostMapping("/post/unlike")
    public ResponseEntity<Post> unlikePost(@RequestParam Long postId, @RequestParam String token) {
        User user = AuthService.getUserFromMap(token);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Post post = vexaService.unlikePost(postId, user.getUsername());
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();
    }
}