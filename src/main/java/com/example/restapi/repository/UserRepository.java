package com.example.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.restapi.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
	public User findByUsername(String username);
	
	}
