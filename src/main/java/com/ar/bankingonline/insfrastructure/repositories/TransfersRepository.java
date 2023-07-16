package com.ar.bankingonline.insfrastructure.repositories;

import com.ar.bankingonline.domain.models.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransfersRepository extends JpaRepository<Transfer, Long> {
}