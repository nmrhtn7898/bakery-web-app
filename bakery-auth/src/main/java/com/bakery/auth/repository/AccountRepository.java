package com.bakery.auth.repository;

import com.bakery.auth.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(attributePaths = {"authorities", "authorities.authority"}, type = FETCH)
    Optional<Account> findByEmail(String email);

}
