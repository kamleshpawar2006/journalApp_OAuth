package net.engineeringdigest.journalApp.security;

import lombok.RequiredArgsConstructor;
import net.engineeringdigest.journalApp.entity.UserEntity;
import net.engineeringdigest.journalApp.service.UserDetailsImpl;
import net.engineeringdigest.journalApp.service.UserService;
import net.engineeringdigest.journalApp.utility.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserDetailsImpl userDetailsImpl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        String email = (String) attributes.get("email");

        // Check if user exists, else create
        UserEntity user = userService.findByUserName(email);
        if (user == null) {
            user = new UserEntity();
            user.setUserName(email);
            user.setPassword(""); // OAuth doesn't use password
            user.setRoles("USER");
            userService.createUser(user);
        }

        // Load UserDetails and generate your JWT
        UserDetails userDetails = userDetailsImpl.loadUserByUsername(email);
        String jwt = jwtUtil.generateToken(userDetails, user.getUserId());

        // Redirect to frontend with your JWT
        String redirectUrl = "http://localhost:4200/oauth2/redirect?token=" + jwt; // change host and port to match UI application. Example: http://localhost/4200 for angular app.
        // if angular app is stored inside spring boot's static folder then this redirectUrl will have same host as backed server
        // in addition to this, write a controller that return "forward:/index.html" so that Angular can load index.html
        response.sendRedirect(redirectUrl);
    }
}
