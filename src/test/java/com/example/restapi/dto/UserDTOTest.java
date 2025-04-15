package com.example.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserDTOTest {
	
	UserDTO u1;
	UserDTO u2;
	String expected;
	
	@BeforeEach
	public void setUp() {
		u1 = new UserDTO();
		u2 = new UserDTO("xu", "123", "Xabier", "Urrutia", 21);
		expected = "UserDTO [username=xu, password=123, name=Xabier, surnames=Urrutia, age=21]";
	}
	
	 @Test
	    public void getUsernameTest() {
	        u1.setUsername("test");
	        assertEquals("test", u1.getUsername());
	        assertEquals("xu", u2.getUsername());
	    }

	    @Test
	    public void getPasswordTest() {
	        u1.setPassword("1");
	        assertEquals("1", u1.getPassword());
	        assertEquals("123", u2.getPassword());
	    }

	    @Test
	    public void getNameTest() {
	        u1.setName("Erik");
	        assertEquals("Erik", u1.getName());
	        assertEquals("Xabier", u2.getName());
	    }

	    @Test
	    public void getSurnamesTest() {
	        u1.setSurnames("Redondo");
	        assertEquals("Redondo", u1.getSurnames());
	        assertEquals("Urrutia", u2.getSurnames());
	    }

	    @Test
	    public void getAgeTest() {
	        u1.setAge(25);
	        assertEquals(25, u1.getAge());
	        assertEquals(21, u2.getAge());
	    }

	    @Test
	    public void toStringTest() {
	        assertEquals(expected, u2.toString());
	    }

}
