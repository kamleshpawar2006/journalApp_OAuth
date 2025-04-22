package net.engineeringdigest.journalApp.service;

import net.engineeringdigest.journalApp.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDetailsImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.findByUserName(username);

//        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
//                .map(SimpleGrantedAuthority::new) // this part is same as - new SimpleGrantedAuthority(role)
//                .collect(Collectors.toList());

        // Above code can be written as below by creating new method createAuthority()

        if(user != null) {
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(UserDetailsImpl::createAuthority)
                    .collect(Collectors.toList());

            try {
                User userDetails = new User(user.getUserName(), user.getPassword(), authorities);
                return userDetails;
            } catch (BadCredentialsException ex) {
                throw new RuntimeException("Invalid Credentials for user: "+ user.getUserName());
            }
        }
        throw new UsernameNotFoundException("User Not Found");
    }

    // below is alternative to - .map(SimpleGrantedAuthority::new)
    private static SimpleGrantedAuthority createAuthority(String role) {
        return new SimpleGrantedAuthority(role);
    }

}