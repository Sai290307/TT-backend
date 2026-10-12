package com.smartbuilding.filter;

import com.smartbuilding.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// This filter runs BEFORE every API request (except the public ones below).
// Its only job: check the JWT token in the request header.
// Think of it like a security guard at the gate:
//   no token / bad token  -> stop here, reply 401
//   good token            -> remember who the user is, let the request through
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Pages anyone can visit WITHOUT logging in (login page itself must stay open!).
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/", "/health", "/api/auth/login", "/api/auth/register"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Step 1: public page? Let it through, no token needed.
        if (isPublicPath(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        // Step 2: read the header. It must look like:  Authorization: Bearer <long-token-text>
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            reject(response, "Missing or invalid Authorization header");
            return;
        }
        String token = header.substring(7); // cut off the "Bearer " part

        // Step 3: is the token real and not expired?
        if (!jwtUtil.validateToken(token)) {
            reject(response, "Invalid or expired token");
            return;
        }

        // Step 4: token is good. Save "who is calling" so controllers can use it.
        // Controllers read it like this:  @RequestAttribute Long userId
        request.setAttribute("username", jwtUtil.extractUsername(token));
        request.setAttribute("userId", jwtUtil.extractUserId(token));

        // Step 5: let the request continue to the controller.
        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    // Sends a small JSON error with status 401 (Unauthorized).
    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}