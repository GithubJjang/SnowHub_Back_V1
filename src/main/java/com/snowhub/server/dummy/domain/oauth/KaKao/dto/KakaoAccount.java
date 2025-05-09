package com.snowhub.server.dummy.domain.oauth.KaKao.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoAccount {
    private String email;
    private Profile profile;

}
