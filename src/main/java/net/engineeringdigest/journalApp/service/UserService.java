package net.engineeringdigest.journalApp.service;

import lombok.RequiredArgsConstructor;
import net.engineeringdigest.journalApp.entity.UserEntity;
import net.engineeringdigest.journalApp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    private static final PasswordEncoder myPasswordEncoder = new BCryptPasswordEncoder();


    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

    public UserEntity findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserEntity findByUserName(String userName) {
        return userRepository.findByUserName(userName).orElse(null);
    }

    public UserEntity createUser(UserEntity user) {
        user.setPassword(myPasswordEncoder.encode(user.getPassword()));
        user.setRoles("USER");
        return userRepository.save(user);
    }

    public UserEntity saveUser(UserEntity user) {
        user.setPassword(myPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public UserEntity saveUserNotPassword(UserEntity user) {
        return userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public Integer getUserIdByJournalId(int journalId) {
        Optional<UserEntity> user = userRepository.findByUserByJournalId(journalId);
        if(user.isPresent()) {
            return user.get().getUserId();
        } else {
            return null;
        }
    }
}
