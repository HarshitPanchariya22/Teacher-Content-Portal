package com.portal.teachercontentportal.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.portal.teachercontentportal.model.User;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    // Rather than using User user = findByTeacherId(String teacherId)  using optional
    // stops the null pointer exception -> optional is a box which may contain value or may be empty
    Optional<User> findByTeacherId(String teacherId);
}
