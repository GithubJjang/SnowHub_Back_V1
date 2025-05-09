package com.snowhub.server.dummy.domain.oauth.firebase.dto;

import lombok.Getter;
import lombok.Setter;

public class FireBaseDetails {
    @Getter
    @Setter
    public static class Token{
        private String displayName;
        private String idToken;
        private String refreshToken; // refreshToken은 DB에 저장.

    }

    @Getter
    @Setter
    public static class ReissuanceToken{
        // 재발급받은 토큰
        String id_token;
        String refresh_token;
    }

}
