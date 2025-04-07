package com.example.restapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.restapi.dto.UserDTO;

public class UserTest {

	User u1;
	User u2;
	User u3;
	User u4;
	UserDTO uDto;
	String expected;
	
	@BeforeEach
	public void setUp() {
		u1 = new User();
		u2 = new User("PeralAitor1", "123");
		u3 = new User("PeralAitor1", "123", "Aitor", "Peral", 21);
		uDto = new UserDTO("PeralAitor2", "123", "Aitor", "Peral", 21);
		u4 = new User(uDto);
		expected = "User [username=PeralAitor2, password=123, name=Aitor, surnames=Peral, age=21]";
	}
	
	@Test
	public void getNameTest() {
		u1.setName("Aitor");
		assertEquals("Aitor", u1.getName());
	}
	
	@Test
	public void getSurnamesTest() {
		u1.setSurnames("Peral");
		assertEquals("Peral", u1.getSurnames());
	}
	
	@Test
	public void getAgeTest() {
		u1.setAge(21);
        assertEquals(21, u1.getAge());
    }
	
	@Test
	public void getUsernameTest() {
		u1.setUsername("Aitor");
		assertEquals("Aitor", u1.getUsername());
	}
	
	@Test
	public void getPasswordTest() {
		u1.setPassword("123");
		assertEquals("123", u1.getPassword());
	}
	
	@Test
	public void hashCodeAndEqualsTest() {
		assertEquals(u2, u3);
		assertEquals(u2.hashCode(), u2.hashCode());
		
		assertNotEquals(u2, u4);
	}
	
	@Test
	public void toStringTest() {
		assertEquals(expected, u4.toString());
	}
	
}
