package com.ras.event_platform.filter;

import com.ras.event_platform.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract the Authorization Header
        String authHeader = request.getHeader("Authorization");

        // 2. If there is no token, let the request pass through.
        // (If the endpoint requires auth, the Controller will crash/reject it anyway because the attribute will be missing).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract and Validate Token
        try {
            String token = authHeader.substring(7); // Remove "Bearer "

            // Fact: This assumes your JwtUtil has a method that parses the token and returns the UUID string.
            String extractedId = jwtUtil.extractSubject(token);

            // 4. THE MAGIC LINE: Attach the verified UUID to the internal request context
            request.setAttribute("teamId", UUID.fromString(extractedId));

        } catch (Exception e) {
            // If the token is expired or tampered with, kill the request immediately.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            response.setContentType("application/json");
            return;
        }

        // 5. Pass the request down the chain to your SubmissionController
        filterChain.doFilter(request, response);
    }
}
