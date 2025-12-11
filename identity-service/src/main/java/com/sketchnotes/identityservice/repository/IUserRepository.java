package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findAllByIsActiveTrue(Pageable pageable);
    Optional<User> findByKeycloakId(String keycloakId);
    Optional<List<User>> findByRole(Role role);
    long countByRole(com.sketchnotes.identityservice.enums.Role role);
    
    // Admin search methods
    Page<User> findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String email, String firstName, String lastName, Pageable pageable);
    
    // Admin search with role filter
    Page<User> findByRole(Role role, Pageable pageable);
    
    Page<User> findByRoleAndEmailContainingIgnoreCaseOrRoleAndFirstNameContainingIgnoreCaseOrRoleAndLastNameContainingIgnoreCase(
            Role role1, String email, Role role2, String firstName, Role role3, String lastName, Pageable pageable);
}
