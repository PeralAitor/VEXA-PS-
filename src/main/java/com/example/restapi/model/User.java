package com.example.restapi.model;

import com.example.restapi.dto.UserDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	private String username;

	private String password;

	private String name;

	private String surnames;

	private int age;

	// No-argument constructor
	public User() {
	}

	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public User(String username, String password, String name, String surnames, int age) {
		super();
		this.username = username;
		this.password = password;
		this.name = name;
		this.surnames = surnames;
		this.age = age;
	}

	public User(UserDTO userDTO) {
		this.username = userDTO.getUsername();
		System.out.println("UserDTO: " + userDTO.getUsername());
		this.password = userDTO.getPassword();
		this.name = userDTO.getName();
		this.surnames = userDTO.getSurnames();
		this.age = userDTO.getAge();
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurnames() {
		return surnames;
	}

	public void setSurnames(String surnames) {
		this.surnames = surnames;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return username.hashCode();
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password + ", name=" + name + ", surnames=" + surnames + ", age=" + age + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return username.equals(((User)obj).getUsername());
	}



}
