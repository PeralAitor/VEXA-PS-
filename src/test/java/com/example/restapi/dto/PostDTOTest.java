package com.example.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PostDTOTest {

	PostDTO p1;
	PostDTO p2;
	String expected;
	
	@BeforeEach
	public void setUp() {
		p1 = new PostDTO();
		p2 = new PostDTO("Adios", "123456789");
		expected = "PostDTO {content='Adios'}";
	}
	
	@Test
	public void getContentTest() {
		p1.setContent("Hola");
		assertEquals(p1.getContent(), "Hola");
		assertEquals(p2.getContent(), "Adios");
	}
	
	@Test
	public void getTokenTest() {
		p1.setToken("987654321");
		assertEquals(p1.getToken(), "987654321");
		assertEquals(p2.getToken(), "123456789");		
	}
	
	@Test
	public void toStringTest() {
		assertEquals(p2.toString(), expected);
	}
	
}
