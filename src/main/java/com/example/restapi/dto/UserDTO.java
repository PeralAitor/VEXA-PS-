package com.example.restapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {
	@JsonProperty("username")
	private String username;

	private String password;

	private String name;

	private String surnames;

	private int age;

	public UserDTO() {

	}


	public UserDTO(String username, String password, String name, String surnames, int age) {
		this.username = username;
		this.password = password;
		this.name = name;
		this.surnames = surnames;
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

	@Override
	public String toString() {
		return "UserDTO [username=" + username + ", password=" + password + ", name=" + name + ", surnames=" + surnames + ", age=" + age + "]";
	}

}
