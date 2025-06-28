package com.group.chat.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "users") // Assuming your existing table is named 'users'
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity { // No longer implements UserDetails

    @Id
    @Column(name = "customer_id", unique = true, nullable = false)
    private String customerId; // Use this as the unique identifier

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Keep password if it's part of your existing user table, even if not used by chat directly
    // If you plan to add login later, it's good to keep it.
    @Column(nullable = false)
    private String password;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Keep role if it helps categorize users for other backend logic
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public enum UserRole {
        CUSTOMER,
        ADMIN
    }
}