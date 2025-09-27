package com.sketchnotes.identityservice.model;

import com.sketchnotes.identityservice.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true )
    private String email;
    @Column(unique = true )
    private String keycloakId;
    private String firstName;
    private String lastName;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean isActive;
    private String avatarUrl;
    private LocalDateTime createAt;
    private  LocalDateTime  updateAt;
}
