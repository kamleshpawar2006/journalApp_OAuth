package net.engineeringdigest.journalApp.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.engineeringdigest.journalApp.service.UserDetailsImpl;
import net.engineeringdigest.journalApp.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    UserDetailsImpl userDetailsImpl;

    // Skip filtering for refresh token endpoint
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/user-registration/refresh-token");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException, ExpiredJwtException, UsernameNotFoundException {
        String authorizationHeader = request.getHeader("Authorization");

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwt = authorizationHeader.substring(7);

                Claims claims = jwtUtil.extractAllClaims(jwt);
                String tokenType = (String) claims.get("tokenType");

                // Only allow ACCESS tokens for regular API requests
                if (!"ACCESS".equals(tokenType)) {
                    throw new SecurityException("Token is not an access token");
                }

                String userName = jwtUtil.extractUsername(jwt);
                UserDetails userDetails = userDetailsImpl.loadUserByUsername(userName);

                List<SimpleGrantedAuthority> authorities = jwtUtil.extractRoles(jwt).stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                if (!jwtUtil.isTokenExpired(jwt)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    response.addHeader("hi", "test header text");
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            // Important: chain.doFilter has to stay inside try
            chain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            // Catch token expiration separately
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token expired. Please login again.\"}");
            response.getWriter().flush();
        } catch (Exception ex) {
            // Catch other exceptions (optional)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token.\"}");
            response.getWriter().flush();
        }
    }

}
