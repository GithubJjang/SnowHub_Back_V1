package com.snowhub.server.dummy.domain.user.infrastructure;

public interface UserRepository {

	User findById(Integer userId);
	User findByDisplayName(String username);// fireBase용
	User findByEmail(String email);
	User findByRefreshToken(String refreshtoken);

	void save(User user);


}
