package com.sketchnotes.identityservice.dto.request;

import com.sketchnotes.identityservice.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class RegisterRequest {
    @Size(min = 4, message = "INVALID_USERNAME")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Password must not be empty")
    private String email;
    @Size(min = 6, message = "INVALID_PASSWORD")
    @NotBlank(message = "Password must not be empty")
    private String password;
    private String firstName;
    private String lastName;
    private String avatarUrl;
}
