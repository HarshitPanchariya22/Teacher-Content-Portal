package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.repository.UserRepository;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.springframework.stereotype.Service;
import com.portal.teachercontentportal.dto.CsvImportResult;
import com.portal.teachercontentportal.model.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
     public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder)
     {
         this.userRepository=userRepository;
         this.passwordEncoder=passwordEncoder;
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

    public CsvImportResult importTeachersFromCsv(MultipartFile file)
    {
        if(file.isEmpty())
        {
            throw new RuntimeException("Empty file!");
        }
        int successCount=0;
        List<String> errors=new ArrayList<>();

        try(BufferedReader reader=new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line;
            boolean firstLine=true;
            while((line= reader.readLine())!=null)
            {
                if(firstLine)
                {
                    firstLine=false;
                    continue;
                }
                if(line.isBlank())
                {
                    continue;
                }
                String[] parts=line.split(",", -1);
                if(parts.length<2)
                {
                    errors.add("Invalid row: "+line);
                    continue;
                }

                String userId=parts[0].trim();
                String password=parts[1].trim();

                if(userId.isEmpty() || password.isEmpty())
                {
                    errors.add("Missing serId/password at line: "+line);
                    continue;
                }

                if(userRepository.findByUserId(userId).isPresent())
                {
                    errors.add("User already exsist: "+userId);
                    continue;
                }
                User teacher=new User();
                teacher.setUserId(userId);
                teacher.setPassword(passwordEncoder.encode(password));
                teacher.setRole(Role.TEACHER);
                userRepository.save(teacher);
                successCount+=1;
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(
                    "Failed to process CSV file: " + e.getMessage()
            );
        }
        return new CsvImportResult(successCount, errors);
    }
}
