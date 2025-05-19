package com.deveagles.be15_deveagles_be.features.user.command.repository;

import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findUserByEmail(String email);

  Optional<User> findUserByPhoneNumber(String phoneNumber);
}
