package com.farmatodo.tokenization.repository;

import com.farmatodo.tokenization.domain.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acceso a datos para CardToken.
 */
@Repository
public interface CardTokenRepository extends JpaRepository<CardToken, Long> {

    Optional<CardToken> findByToken(String token);
}
