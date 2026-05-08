package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.model.entity.CustomerEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    boolean existsByFin(String fin);
    boolean existsByVoen(String voen);
    List<CustomerEntity> findAllByIsVisibleTrue();
    @EntityGraph(attributePaths = {"cards", "currentAccounts"})
    Optional<CustomerEntity> findWithCardAndAccountsById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"cards", "currentAccounts"})
    @Query("SELECT c FROM CustomerEntity c WHERE c.id = :id")
    Optional<CustomerEntity> findByIdForUpdate(@Param("id") Long id);
    Optional<CustomerEntity> findByRegistrationToken(String registrationToken);
}

