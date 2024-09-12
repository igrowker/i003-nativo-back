package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation,Long> {

}
