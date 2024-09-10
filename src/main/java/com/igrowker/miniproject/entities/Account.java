package com.igrowker.miniproject.entities;

import jakarta.persistence.Entity;

@Entity
public class Account {

    private Long id;

    private Long amount;

    private boolean enabled;
}
