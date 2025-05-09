package com.snowhub.server.dummy.security.error;

import lombok.Getter;


@Getter
public enum ErrorType {
    Not_Valid_Token("Failed to parse Firebase ID token. Make sure you passed a string that represents a complete and valid JWT. See https://firebase.google.com/docs/auth/admin/verify-id-tokens for details on how to retrieve an ID token."),
    Token_Expiration("Firebase ID token has expired. Get a fresh ID token and try again. See https://firebase.google.com/docs/auth/admin/verify-id-tokens for details on how to retrieve an ID token."),
    Parse_Error("Failed to parse Firebase ID token. Make sure you passed a string that represents a complete and valid JWT. See https://firebase.google.com/docs/auth/admin/verify-id-tokens for details on how to retrieve an ID token.")
    ;

    private final String value;

    ErrorType(String value) {
        this.value=value;
    }
}
