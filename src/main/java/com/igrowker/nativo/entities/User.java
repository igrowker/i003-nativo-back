package com.igrowker.nativo.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true , nullable = false)
    private Long dni;

    private String name;

    private String surname;

    @Column(unique =true , nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean enabled;

    private String phone;

    @OneToOne
    private Account account;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt =  LocalDateTime.now();
    }

}
