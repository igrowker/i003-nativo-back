package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
        Optional<Account> findByUserId(String userId);


}
