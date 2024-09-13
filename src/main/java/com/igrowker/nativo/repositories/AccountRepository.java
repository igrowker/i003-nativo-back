package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
