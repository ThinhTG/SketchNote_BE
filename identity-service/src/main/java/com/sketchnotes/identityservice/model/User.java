package com.sketchnotes.identityservice.model;

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
    private LocalDateTime createAt;
    private  LocalDateTime  updateAt;
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> sentMessages;
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
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

    // Helper method to get max projects allowed (3 for free, unlimited for subscription)
    public int getMaxProjects() {
        return hasActiveSubscription() ? -1 : 3; // -1 means unlimited
    }
}
