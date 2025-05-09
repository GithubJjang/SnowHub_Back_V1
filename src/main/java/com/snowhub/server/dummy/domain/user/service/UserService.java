package com.snowhub.server.dummy.domain.user.service;

import com.snowhub.server.dummy.common.exception.CustomException;
import com.snowhub.server.dummy.common.exception.ErrorCode;
import com.snowhub.server.dummy.domain.user.infrastructure.User;
import com.snowhub.server.dummy.domain.user.entity.UserEntity;
import com.snowhub.server.dummy.domain.user.infrastructure.UserRepository;
import com.snowhub.server.dummy.domain.user.model.request.UserRequest;
import com.snowhub.server.dummy.domain.role.RoleType;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;


    // 1. 회원가입(Firebase)
    @Transactional
    public void saveUser(UserRequest userRequest){

        String rawPassword = userRequest.getPassword();
        String encodePassword = passwordEncoder.encode(rawPassword);
        userRequest.setRoleType(RoleType.user);

        userRequest.setPassword(encodePassword);

        UserEntity userEntity = UserEntity.toEntity(userRequest);

        User user = User.from(userEntity);

        userRepository.save(user);

    }

    // 3. 이메일기반으로 검색.
    @Transactional
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 4. refreshToken일치여부 찾기
    @Transactional
    public User findRefreshToken(String refeshToken){
        return userRepository.findByRefreshToken(refeshToken);

    }

    // 5. 기존 refeshToken을 새로 업데이트.
    @Transactional
    public void updateRefreshToken(String refeshToken,String email){
        User findUser = Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> // 일치하는 email이 없는 경우 등록된 사용자가 x -> 에러발생.
                        new CustomException(ErrorCode.USER_EMAIL_NOT_FOUND));
        findUser.setRefreshToken(refeshToken);
    }

    // 6. uid 업데이트
    @Transactional
    public void updateUid(String email, String uid){
        User findUser = Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> // 일치하는 email이 없는 경우 등록된 사용자가 x -> 에러발생.
                    new CustomException(ErrorCode.USER_EMAIL_NOT_FOUND));
        findUser.setUid(uid);
    }

}
