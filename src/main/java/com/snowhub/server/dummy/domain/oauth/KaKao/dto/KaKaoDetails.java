package com.snowhub.server.dummy.domain.oauth.KaKao.dto;

import com.snowhub.server.dummy.domain.oauth.KaKao.dto.KakaoAccount;

import lombok.Getter;
import lombok.Setter;

public class KaKaoDetails {

    @Getter
    @Setter
    public static class Token{
        private String token_type;
        private String  access_token;
        private String  id_token;
        private String  refresh_token;
    }

    @Getter
    @Setter
    public static class User{
        private String connected_at; // <-DateTime
        private KakaoAccount kakao_account;
        private long id;
    }


}
