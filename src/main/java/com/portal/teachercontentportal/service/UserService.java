package com.portal.teachercontentportal.service;
import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }
    public User RegisterUser(User user)
    {
        return userRepository.save(user);
    }
    public Optional<User> getTeacherByTeacherId(String TeacherId)
    {
        return userRepository.findByTeacherId(TeacherId);
    }
}
