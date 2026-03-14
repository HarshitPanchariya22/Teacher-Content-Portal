package com.portal.teachercontentportal.service;
import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // injecting the passwordEncoder object
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
//    public User RegisterUser(@NonNull User user)
//    {
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        return userRepository.save(user);
//    }
    public Optional<User> getUserByUserId(String userId)
    {
        return userRepository.findByUserId(userId);
    }

    public User authenticateUser(String userId, String password)
    {
        User user = userRepository.findByUserId(userId).orElseThrow(()-> new RuntimeException("User not found"));
        if(!passwordEncoder.matches(password, user.getPassword()))
        {
            throw new RuntimeException("Password Invalid");
        }
        return user;

    }
}
