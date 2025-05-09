package com.snowhub.server.dummy.domain.oauth.google.dto;

import lombok.Getter;
import lombok.Setter;


public class GoogleDetails {

    @Getter
    @Setter
    public static class Token{
        private String access_token;
        private int expires_in;
        private String scope;
        private String token_type;
        private String id_token;
        private String refresh_token;

    }
    @Getter
    @Setter
    public static class User{
        private String email;
        private String id;
        private String name;

    }
}
