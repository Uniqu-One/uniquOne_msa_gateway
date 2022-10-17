package com.sparos.uniquone.msagateway.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
public class JwtToken {
    private String token;
    private String refreshToken;

    public JwtToken(String token, String refreshToken){
        this.token = token;
        this.refreshToken = refreshToken;
    }

}
