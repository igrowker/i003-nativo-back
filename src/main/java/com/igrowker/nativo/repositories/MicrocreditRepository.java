package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Microcredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MicrocreditRepository extends JpaRepository<Microcredit, Long> {
}
