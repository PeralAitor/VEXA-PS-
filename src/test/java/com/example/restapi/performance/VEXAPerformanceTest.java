// src/test/java/com/example/restapi/performance/PerformanceTest.java
package com.example.restapi.performance;

import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;

import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.restapi.service.AuthService;
import com.example.restapi.service.VEXAService;
import com.example.restapi.dto.*;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;

@SpringBootTest
@ExtendWith(JUnitPerfInterceptor.class)
public class VEXAPerformanceTest {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private VEXAService vexaService;
    
    private static String validToken;
    private static final String TEST_USER = "testuser";
    private static final String TEST_PASS = "Testpass123!";

    @JUnitPerfTestActiveConfig
    private static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator(System.getProperty("user.dir") + "/target/reports/perf-report.html"))
            .build();

    @BeforeEach
    void setup() {
        // Registrar usuario y obtener token
        authService.register(new UserDTO(TEST_USER, TEST_PASS, "Test", "User", 25));
        validToken = authService.login(new CredentialsDTO(TEST_USER, TEST_PASS));
        
        // Crear un post inicial para update/delete
        PostDTO initialPost = new PostDTO("Initial content", validToken);
        vexaService.createPost(initialPost, validToken);
    }

    @AfterEach
    void cleanup() {
        // Limpiar datos después de cada test
        authService.logout(validToken);
    }

    // Auth Endpoints
    @Test
    @JUnitPerfTest(threads = 50, durationMs = 10000, warmUpMs = 2000)
    @JUnitPerfTestRequirement(executionsPerSec = 100, percentiles = "95:200ms")
    void testLoginPerformance() {
        authService.login(new CredentialsDTO(TEST_USER, TEST_PASS));
    }

    @Test
    @JUnitPerfTest(threads = 30, durationMs = 8000)
    @JUnitPerfTestRequirement(executionsPerSec = 50, allowedErrorPercentage = 0.1f)
    void testRegisterPerformance() {
        String username = "user_" + System.currentTimeMillis();
        authService.register(new UserDTO(username, "Password123!", "Name", "Surname", 30));
    }

    @Test
    @JUnitPerfTest(threads = 40, durationMs = 5000)
    void testLogoutPerformance() {
        authService.logout(validToken);
    }

    // Post Endpoints
    @Test
    @JUnitPerfTest(threads = 20, durationMs = 10000)
    @JUnitPerfTestRequirement(percentiles = "90:150ms,95:200ms")
    void testCreatePostPerformance() {
        PostDTO post = new PostDTO("Test content", validToken);
        vexaService.createPost(post, validToken);
    }

    @Test
    @JUnitPerfTest(threads = 25, durationMs = 15000)
    void testGetPostsPerformance() {
        vexaService.getPosts(validToken);
    }

    @Test
    @JUnitPerfTest(threads = 15, durationMs = 10000)
    void testGetPostsByUserPerformance() {
        vexaService.getPostsByUser(validToken);
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 8000, warmUpMs = 1000)
    @JUnitPerfTestRequirement(percentiles = "95:500ms", allowedErrorPercentage = 1.0f)
    void testUpdatePostPerformance() {
        List<Post> posts = vexaService.getPostsByUser(validToken);
        if (!posts.isEmpty()) {
            Post post = posts.get(0); // Usa el primer post creado
            post.setContent("Updated content - " + System.currentTimeMillis());
            vexaService.updatePost(post);
        }
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 6000)
    void testDeletePostPerformance() {
        Post post = new Post();
        post.setId(1L); // Asume que existe un post con ID 1
        vexaService.deletePost(post);
    }
}