package com.ridevpn.ridevpn.repository;

import com.ridevpn.ridevpn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    @Query("select u.vpnIp from User u where u.vpnIp is not null") // check if null because users who have never connected wont have an IP.
    List<String> findAllVpnIps();
}
