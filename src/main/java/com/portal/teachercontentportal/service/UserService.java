package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
     public UserService(UserRepository userRepository)
     {
         this.userRepository=userRepository;
     }

     public List<User> getAllUsers()
     {
         return userRepository.findAll();
     }

     public User getUserById(Long id)
     {
         return userRepository.findById(id).orElseThrow(()->new RuntimeException("User with id "+id+" not found"));
     }

     public void deleteUser(Long id)
     {
         userRepository.deleteById(id);
     }


}
