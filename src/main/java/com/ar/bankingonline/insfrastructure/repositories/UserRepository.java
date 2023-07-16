package com.ar.bankingonline.insfrastructure.repositories;

import com.ar.bankingonline.domain.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

}
