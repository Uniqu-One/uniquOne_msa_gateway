package com.sparos.uniquone.msagateway.filter;

import com.sparos.uniquone.msagateway.utils.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    //yml 파일에서 특정 정보 가져오기위해서.

    Environment env;

    public static class Config{

    }

    @Autowired
    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            //토큰에 관한 검사 처리.
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
//            ServerHttpResponse response = exchange.getResponse();
            //토큰이 없다면!
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);
            }

            //bearer 토큰이 저장되어있음.
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            //bearer 제거해주고 빈값을 해주면 나머지가 토큰정보.
            String jwt = authorizationHeader.replace("Bearer", "");

            //토큰 검증.
//            if(!isJwtValid(jwt)){
            if(!JwtProvider.isJwtValid(jwt)){
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }
//            String token = request.getQueryParams().get("token").get(0);
//            String userRoleByRequest = JwtProvider.getUserRoleByRequest(request);
//            if(userRoleByRequest.equals("ROLE_USER")){
//                response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
//                response.getHeaders().setLocation(URI.create("/login"));
//                response.setComplete();
//            }

            return chain.filter(exchange);
        });
    }


    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
//        ServerHttpResponse response = exchange.getResponse();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error(err);

        return response.setComplete();
    }
}
