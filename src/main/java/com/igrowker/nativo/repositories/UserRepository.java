package com.igrowker.nativo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.igrowker.nativo.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>{

    Optional<User> findByEmail(String email);
    Optional<User> findByDni(Long dni);
    
}