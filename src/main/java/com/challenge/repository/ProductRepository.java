package com.challenge.repository;

import com.challenge.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Product}
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndSellerId(UUID productId, UUID sellerId);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findByIdPessimistic(UUID id);
}
