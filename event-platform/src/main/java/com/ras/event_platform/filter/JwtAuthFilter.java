package com.ras.event_platform.filter;

import com.ras.event_platform.util.JwtUtil;
import com.ras.event_platform.model.User;
import com.ras.event_platform.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);
            String extractedSubject = jwtUtil.extractSubject(token);
            
            if ("ROLE_TEAM".equals(role)) {
                if (path.startsWith("/api/admin") || path.startsWith("/api/evaluate")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\": \"Teams are not allowed to access this endpoint.\"}");
                    response.setContentType("application/json");
                    return;
                }
                request.setAttribute("teamId", UUID.fromString(extractedSubject));
            } else if ("ROLE_ADMIN".equals(role)) {
                if (path.startsWith("/api/submit") || path.startsWith("/api/status") || path.startsWith("/api/submissions")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\": \"Admins cannot submit projects.\"}");
                    response.setContentType("application/json");
                    return;
                }
                Optional<User> userOpt = userRepository.findByUsername(extractedSubject);
                if (userOpt.isPresent()) {
                    request.setAttribute("userId", userOpt.get().getId());
                } else {
                    throw new RuntimeException("Admin user not found");
                }
            } else {
                throw new RuntimeException("Unknown role");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            response.setContentType("application/json");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
