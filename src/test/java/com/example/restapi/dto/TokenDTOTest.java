package com.example.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TokenDTOTest {

	TokenDTO t1;
	
	@BeforeEach
	public void setUp() {
		t1 = new TokenDTO();
	}
	
	@Test
	public void getTokenTest() {
		t1.setToken("123456789");
		assertEquals(t1.getToken(), "123456789");
	}
}
