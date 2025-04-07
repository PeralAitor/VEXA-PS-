package com.example.restapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.restapi.dto.PostDTO;

public class PostTest {
	
	Post p1;
	Post p2;
	Post p3;
	Post p4;
	Post p5;
	Post p6;
	String expected;
	
	@BeforeEach
	public void setUp() {
		p1 = new Post();
		p1.setId(1);
		p1.setOwner("owner");
		p1.setContent("content");
		p1.setDate(new Date());
		
		p2 = new Post();
		p2.setDate(null);
		
		p3 = new Post("Hola", "Aitor", new Date());
		p3.setId(1);
		
		p4 = new Post("Adios", "Aitor", new Date());
		p4.setId(1);
		
		p5 = new Post("Otra cosa", "Otro", new Date());
        p5.setId(2L);
        
        expected = "Post [owner=Aitor, content=Hola, date=" + new Date().toString() + "]";
        
        PostDTO pDto = new PostDTO("Hola", "12g345h6j");
        User owner = new User("Aitor", "123");
        p6 = new Post(pDto, owner);
	}
	
	@Test
	public void getIdTest() {
		assertEquals(1, p1.getId());
	}
	
	@Test
	public void getOwnerTest() {
		assertEquals("owner", p1.getOwner());
	}
	
	@Test
	public void getContentTest() {
		assertEquals("content", p1.getContent());
	}
	
	@Test
	public void getDateTest() {
		p1.setDate(new Date());
		assertEquals(new Date(), p1.getDate());
	}
	
	@Test
	public void getFormattedDateTest() {
		assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(p1.getDate()), p1.getFormattedDate());
		assertEquals("Fecha no disponible", p2.getFormattedDate());
	}
	
	@Test
    public void hashCodeAndEqualsTest() {
        assertEquals(p3, p4, "Dos posts con el mismo ID deben ser iguales");
        assertEquals(p3.hashCode(), p4.hashCode(), "Dos posts iguales deben tener el mismo hashCode");

        assertNotEquals(p3, p5, "Posts con distinto ID no deben ser iguales");
        assertNotEquals(p3.hashCode(), p5.hashCode(), "Posts distintos deben tener distinto hashCode (aunque no es obligatorio)");
    }
	
	@Test
	public void toStringTest() {
        assertEquals(expected, p6.toString());
	}
	
}
