package com.ridevpn.ridevpn.repository;

import com.ridevpn.ridevpn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
}
