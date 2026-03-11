package com.portal.teachercontentportal.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.portal.teachercontentportal.model.User;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTeacherId(String teacherId);
}
