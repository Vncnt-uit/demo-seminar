package com.project.ecommerce.repository;

import com.project.ecommerce.model.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// =============================================================================
// [UML: Package Diagram] package «repository» -> ProductRepository
// [ARCH: MONOLITH] InventoryService uses this repository directly on the shared
// products table. The @Lock is a LOCAL database row lock -- possible because
// there is only one database and one transaction.
// =============================================================================
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductEntity p where p.id = :id")
    Optional<ProductEntity> findByIdForUpdate(@Param("id") Long id);
}
