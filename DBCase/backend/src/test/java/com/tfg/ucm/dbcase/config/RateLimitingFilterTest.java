package com.tfg.ucm.dbcase.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tfg.ucm.dbcase.controller.UserController;
import com.tfg.ucm.dbcase.service.AuthService;
import com.tfg.ucm.dbcase.service.JWTService;
import com.tfg.ucm.dbcase.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = UserController.class)
@Import({RateLimitingFilter.class})
class RateLimitingFilterTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @MockitoBean private JWTService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UserController userController;

    @Test
    @WithMockUser
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testRateLimitingAllowsRequest() throws Exception {
        mockMvc.perform(get("/api/user/me")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testRateLimitingExceeded() throws Exception {
        // First MAX_NUMBER_REQUESTS will go through
        for (int i = 0; i < RateLimitingFilter.MAX_NUMBER_REQUESTS; i++) {
            mockMvc.perform(get("/api/user/me")).andExpect(status().isOk());
        }
        // The next one should be blocked
        mockMvc.perform(get("/api/user/me")).andExpect(status().isTooManyRequests());
    }
}
