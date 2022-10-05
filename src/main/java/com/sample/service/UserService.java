package com.sample.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.sample.model.User;
import com.sample.web.dto.UserRegistrationDto;

public interface UserService extends UserDetailsService{
	User save(UserRegistrationDto registrationDto);
}
