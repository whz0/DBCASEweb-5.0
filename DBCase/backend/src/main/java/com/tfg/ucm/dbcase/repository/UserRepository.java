package com.tfg.ucm.dbcase.repository;

import com.tfg.ucm.dbcase.model.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserById(Long id);

    Optional<User> findByUsername(String username);

    void deleteByExpiresAtBeforeAndExpiresAtIsNotNull(LocalDateTime now);
}
