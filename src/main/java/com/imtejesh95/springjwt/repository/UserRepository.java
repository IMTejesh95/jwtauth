package com.imtejesh95.springjwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imtejesh95.springjwt.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByUsername(String username);
	Boolean existsByUsername(String username);
	Boolean existsByEmail(String email);

}
