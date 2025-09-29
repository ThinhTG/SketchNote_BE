package com.sketchnotes.identityservice.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class TokenRequest {
   private String refreshToken;
}
