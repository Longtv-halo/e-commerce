package com.demo.service;

import com.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

	private final UsersRepository usersRepository;

	public String getAllUsers() {
		return usersRepository.findAll().toString();
	}
}
