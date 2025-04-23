package net.engineeringdigest.journalApp.controller;

import lombok.RequiredArgsConstructor;
import net.engineeringdigest.journalApp.entity.UserEntity;
import net.engineeringdigest.journalApp.service.UserDetailsImpl;
import net.engineeringdigest.journalApp.service.UserService;
import net.engineeringdigest.journalApp.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-registration")
public class UserCreation {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserDetailsImpl userDetailsImpl;

    private static final PasswordEncoder myPasswordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        return new ResponseEntity(userService.createUser(user), HttpStatus.OK);
    }

    @GetMapping("/validateJwt")
    public ResponseEntity<?> validateJwt() {
        Map<String, String> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity userInDB = userService.findByUserName(authentication.getName());
            if(userInDB != null) {
                response.put("profileImage", userInDB.getProfilePicture());
                response.put("status", "Valid Token");
            } else {
                response.put("status", "Invalid User");
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.put("status", "Invalid Token");
            return new ResponseEntity<>(ex, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserEntity user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword())
            );
            UserEntity dbUser = userService.findByUserName(user.getUserName());
            UserDetails userDetails = userDetailsImpl.loadUserByUsername(user.getUserName());
            String jwt = jwtUtil.generateToken(userDetails, dbUser.getUserId());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

}
