package com.sketchnotes.identityservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sketchnotes.identityservice.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


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
    @JsonIgnore
    private String password;
    private LocalDateTime createAt;
    private  LocalDateTime  updateAt;
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    private Integer maxProjects = 3;
    
    // AI Credits management
    @Column(nullable = false, columnDefinition = "integer default 10")
    @Builder.Default
    private Integer aiCredits = 10; // Số credit AI còn lại
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CreditTransaction> creditTransactions;
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Message> sentMessages;
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Message> receivedMessages;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Wallet wallet;
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Blog> blogs;
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserSubscription> subscriptions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VerifyToken> verifyTokens;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<BankAccount> bankAccounts;

    // Helper method to get active subscription
    public UserSubscription getActiveSubscription() {
        if (subscriptions == null) {
            return null;
        }
        return subscriptions.stream()
                .filter(UserSubscription::isCurrentlyActive)
                .findFirst()
                .orElse(null);
    }

    // Helper method to check if user has active subscription
    public boolean hasActiveSubscription() {
        return getActiveSubscription() != null;
    }

}
