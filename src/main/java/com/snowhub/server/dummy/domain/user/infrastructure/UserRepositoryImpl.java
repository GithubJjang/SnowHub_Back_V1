package com.snowhub.server.dummy.domain.user.infrastructure;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository{

	private final UserJpaRepository userJpaRepository;

	// 기본적인 CRUD
	public void save(User user){
		userJpaRepository.save(user);
	}

	@Override
	public User findById(Integer userId) {
		return userJpaRepository.findById(userId)
			.orElseThrow(
				()-> new RuntimeException("There is no userId: "+userId)
			);
	}

	// 커스텀 기능
	@Override
	public User findByDisplayName(String username) {
		return userJpaRepository.findByDisplayName(username);
	}

	@Override
	public User findByEmail(String email) {
		return userJpaRepository.findByEmail(email);
	}

	@Override
	public User findByRefreshToken(String refreshtoken) {
		return userJpaRepository.findByRefreshToken(refreshtoken);
	}

}
