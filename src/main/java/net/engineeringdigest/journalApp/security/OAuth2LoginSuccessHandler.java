package net.engineeringdigest.journalApp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        String registrationId = token.getAuthorizedClientRegistrationId(); // "google" or "github"

        // Logging for debugging
        System.out.println("Login via: " + registrationId);
        attributes.forEach((key, value) -> System.out.println(key + " = " + value));

        String email = null;
        String name = null;
        String pictureUrl = null;

        // Provider-specific mapping
        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            pictureUrl = (String) attributes.get("picture");
        } else if ("github".equals(registrationId)) {
            email = (String) attributes.get("email"); // May be null if email is private
            name = (String) attributes.get("name");
            pictureUrl = (String) attributes.get("avatar_url");

            // Fallback if name is null
            if (name == null) {
                name = (String) attributes.get("login"); // GitHub username
            }

            // Fallback if email is null (you may need a second API call to get email)
            if (email == null) {
                email = name + "@users.noreply.github.com"; // fallback dummy email
            }
        }

        // Check if user exists
        UserEntity user = userService.findByUserName(email);
        if (user == null) {
            user = new UserEntity();
            user.setUserName(email);
            user.setPassword(""); // OAuth doesn't use password
            user.setRoles("USER");
            user.setName(name);
            user.setEmail(email);
            user.setProfilePicture(pictureUrl);
            userService.createUser(user);
        } else {
            user.setName(name);
            user.setEmail(email);
            user.setProfilePicture(pictureUrl);
            userService.saveUser(user);
        }

        // Load UserDetails and generate JWT tokens
        UserDetails userDetails = userDetailsImpl.loadUserByUsername(email);
        String jwt = jwtUtil.generateToken(userDetails, user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Redirect to frontend with token
        String redirectUrl = "http://localhost:4200/login?token=" + jwt + "&refresh=" + refreshToken;
        response.sendRedirect(redirectUrl);
    }
}
