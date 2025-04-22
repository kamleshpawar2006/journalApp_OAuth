package net.engineeringdigest.journalApp.filter;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import net.engineeringdigest.journalApp.service.UserDetailsImpl;
import net.engineeringdigest.journalApp.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException, ExpiredJwtException, UsernameNotFoundException {
        String authorizationHeader = request.getHeader("Authorization");
        try {
            if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwt = authorizationHeader.substring(7);
                String userName = null;
                try {
                    userName = jwtUtil.extractUsername(jwt);
                } catch (ExpiredJwtException ex) {
                    log.error(ex.getMessage());
                    throw new ExpiredJwtException(ex.getHeader(), ex.getClaims(),ex.getMessage());
                }

                UserDetails userDetails;
                try {
                    userDetails = userDetailsImpl.loadUserByUsername(userName);
                } catch (UsernameNotFoundException ex) {
                    log.error(ex.getMessage());
                    throw new UsernameNotFoundException(ex.getMessage());
                }

                List<String> roles = jwtUtil.extractRoles(jwt);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                if(!jwtUtil.isTokenExpired(jwt)) {
//                below statements can be written as
//                Because sometimes only username is needed (lightweight cases).
//                And sometimes full user object is needed (secure, complex systems).
//                Spring gives freedom.
//                Example:
//                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),userDetails.getPassword(), authorities);
//                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, authorities);
//                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
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
