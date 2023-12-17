package com.app.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.app.model.User;
import com.app.dto.UserRegistrationDto;

public interface UserService extends UserDetailsService{
	User save(UserRegistrationDto registrationDto);
}
