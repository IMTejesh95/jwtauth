package com.imtejesh95.springjwt.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imtejesh95.springjwt.entity.ERole;
import com.imtejesh95.springjwt.entity.Role;
import com.imtejesh95.springjwt.entity.User;
import com.imtejesh95.springjwt.payload.request.LoginRequest;
import com.imtejesh95.springjwt.payload.request.SignupRequest;
import com.imtejesh95.springjwt.payload.response.JwtResponse;
import com.imtejesh95.springjwt.payload.response.MessageResponse;
import com.imtejesh95.springjwt.repository.RoleRepository;
import com.imtejesh95.springjwt.repository.UserRepository;
import com.imtejesh95.springjwt.security.jwt.JwtUtils;
import com.imtejesh95.springjwt.security.service.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JwtUtils jwtUtils;
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser( @Valid @RequestBody LoginRequest loginRequest ){
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(a -> a.getAuthority())
				.collect(Collectors.toList());
		
		
		return ResponseEntity.ok( new JwtResponse(
				jwt,
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				roles)) ;
	}
	
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser( @Valid @RequestBody SignupRequest signupRequest ){
		if( userRepository.existsByUsername(signupRequest.getUsername()) ) {
			return ResponseEntity.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}
		
		if( userRepository.existsByEmail(signupRequest.getEmail()) ) {
			return ResponseEntity.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}
		
		// Create new user's account
		User user = new User(signupRequest.getUsername(), 
				signupRequest.getEmail(), 
				passwordEncoder.encode(signupRequest.getPassword()) );
		
		System.out.println("### Role: "+ signupRequest.toString());
		List<String> strRoles = signupRequest.getRole();
		
		Set<Role> roles = new HashSet<Role>();
		
		if( strRoles == null ) {
			Role role = roleRepository.findByName(ERole.ROLE_USER);
			roles.add(role);
		}else{
			strRoles.forEach(role -> {
				switch(role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN);
					roles.add(adminRole);
					break;
					
				case "mod":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR);
					roles.add(modRole);
					break;
					
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER);
					roles.add(userRole);
				}
				
			});
		}
		
		user.setRoles(roles);
		userRepository.save(user);
		
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
	
	
}
