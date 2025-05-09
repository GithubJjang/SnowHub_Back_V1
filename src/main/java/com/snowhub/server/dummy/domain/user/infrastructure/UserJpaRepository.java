package com.snowhub.server.dummy.domain.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User,Integer> {

    User findByDisplayName(String username);// fireBase용
    User findByEmail(String email);
    User findByRefreshToken(String refreshtoken);
}
