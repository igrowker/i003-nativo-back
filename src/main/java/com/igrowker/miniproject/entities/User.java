package com.igrowker.miniproject.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    private Long id;

    private Long dni;

    private String name;

    private String surname;

    private String email;

    private String password;

    private boolean enabled;

    private String phone;

    private Account account;

}
