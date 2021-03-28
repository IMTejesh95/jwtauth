package com.imtejesh95.springjwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imtejesh95.springjwt.entity.ERole;
import com.imtejesh95.springjwt.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

	Role findByName(ERole name);
}
