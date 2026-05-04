package com.portal.teachercontentportal.controller;

import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.service.UserService;
import com.portal.teachercontentportal.dto.CsvImportResult;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService)
    {
        this.userService=userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers()
    {
        List<User> users=userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id)
    {
        User user=userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id)
    {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/import-teachers")
    public ResponseEntity<CsvImportResult> importTeachers(@RequestParam("file") MultipartFile file)
    {
        CsvImportResult result=userService.importTeachersFromCsv(file);
        return ResponseEntity.ok(result);
    }
}
