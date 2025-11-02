package com.sketchnotes.identityservice.dtos.identity;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LoginExchangeResponse {
    String accessToken;
    int expiresIn;
    int refreshExpiresIn;
    String refreshToken;
    String tokenType;
    String idToken;
    int notBeforePolicy;
    String sessionState;
    String scope;

}
