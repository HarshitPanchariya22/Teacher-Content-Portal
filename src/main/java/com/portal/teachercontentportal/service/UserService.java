package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.model.Branch;
import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.model.Year;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.portal.teachercontentportal.dto.CsvImportResult;
import com.portal.teachercontentportal.model.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    public CsvImportResult importUsersFromCsv(MultipartFile file)
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
            int lineNumber=0;
            while((line= reader.readLine())!=null)
            {
                lineNumber+=1;
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
                if(parts.length<3)
                {
                    errors.add("Invalid row: "+lineNumber);
                    continue;
                }

                String userId=parts[0].trim();
                String password=parts[1].trim();
                String roleText=parts[2].trim();

                if(userId.isEmpty())
                {
                    errors.add("Missing userId at line: "+lineNumber);
                    continue;
                }
                if(password.isEmpty())
                {
                    errors.add("Missing password at line: "+lineNumber);
                    continue;
                }
                if(roleText.isEmpty())
                {
                    errors.add("Missing role at line: "+lineNumber);
                    continue;
                }

                if(userRepository.findByUserId(userId).isPresent())
                {
                    errors.add("User already exsist: "+userId);
                    continue;
                }

                Role role;
                try{
                    role=Role.valueOf(roleText.toUpperCase());
                }
                catch (Exception e){
                    errors.add("Invalid role at line: "+lineNumber);
                    continue;
                }
                if(role==Role.ADMIN)
                {
                    errors.add("ADMIN cannot be imported from CSV at line: "+lineNumber);
                    continue;
                }
                User user=new User();
                user.setUserId(userId);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(role);
                if(role==Role.STUDENT)
                {
                    if(parts.length<5)
                    {
                        errors.add("Incomplete field at: "+lineNumber);
                        continue;
                    }
                    String year=parts[3].trim();
                    String branch=parts[4].trim();
                    if(year.isEmpty())
                    {
                        errors.add("Missing year at line: "+lineNumber);
                        continue;
                    }
                    if(branch.isEmpty())
                    {
                        errors.add("Missing branch at line: "+lineNumber);
                        continue;
                    }
                    try{
                        user.setYear(Year.valueOf(year.toUpperCase()));
                    }
                    catch (Exception e){
                        errors.add("Invalid year at line: "+lineNumber);
                        continue;
                    }
                    try{
                        user.setBranch(Branch.valueOf(branch.toUpperCase()));
                    }
                    catch (Exception e){
                        errors.add("Invalid branch at line: "+lineNumber);
                        continue;
                    }
                }
                userRepository.save(user);
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
