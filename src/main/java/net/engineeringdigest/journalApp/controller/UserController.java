package net.engineeringdigest.journalApp.controller;

import lombok.RequiredArgsConstructor;
import net.engineeringdigest.journalApp.entity.UserEntity;
import net.engineeringdigest.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        List<UserEntity> users = userService.getAll();
        if(users.size() > 0) {
            return new ResponseEntity<>(users, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{userName}")
    public ResponseEntity<UserEntity> getUserByName(@PathVariable String userName) {
        UserEntity user = userService.findByUserName(userName);
        if(user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/validateJwt")
    public ResponseEntity<?> validateJwt() {
        Map<String, String> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity userInDB = userService.findByUserName(authentication.getName());
            response.put("status", "valid");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.put("status", "Invalid");
            return new ResponseEntity<>(ex, HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping
    public ResponseEntity<HttpStatus> updateUser(@RequestBody UserEntity user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userInDB = userService.findByUserName(authentication.getName());
        if(!userInDB.equals(null)) {
            userInDB.setPassword(user.getPassword());
            userService.saveUser(userInDB);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Integer id) {
        try {
            UserEntity user = userService.findById(id);
            if(!user.equals(null)) {
                userService.deleteUser(id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
