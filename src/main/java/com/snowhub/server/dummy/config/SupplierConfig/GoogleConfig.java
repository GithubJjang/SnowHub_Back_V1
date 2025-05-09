package com.snowhub.server.dummy.config.SupplierConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;


@Setter
@Getter
@NoArgsConstructor  // 기본 생성자 필요
@ConfigurationProperties("google")
public class GoogleConfig {
    private String ClientId;
    private String ClientSecret;
    private String RedirectUri;
}
