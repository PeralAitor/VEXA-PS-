package com.example.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CredentialsDTOTest {
	
	CredentialsDTO c1;
	CredentialsDTO c2;
	
	@BeforeEach
	public void setUp() {
		c1 = new CredentialsDTO();
		c2 = new CredentialsDTO("xu", "123");
	}
	
	@Test
	public void getUsernameTest() {
		c1.setUsername("test");
		assertEquals(c1.getUsername(), "test");
		assertEquals(c2.getUsername(), "xu");
	}
	
	@Test
	public void getPasswordTest() {
		c1.setPassword("1");
		assertEquals(c1.getPassword(), "1");
		assertEquals(c2.getPassword(), "123");
	}

}
