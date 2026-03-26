package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.model.entity.CurrentAccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CurrentAccountRepository extends JpaRepository<CurrentAccountEntity, Long> {
    @EntityGraph(attributePaths = {"customer"})
    List<CurrentAccountEntity> findByCustomerId(Long customerId);
    @EntityGraph(attributePaths = {"customer"})
    Optional<CurrentAccountEntity> findWithCustomerById(Long id);
    long countByCustomerId(Long customerId);
    boolean existsByAccountNumber(String accountNumber);
    @EntityGraph(attributePaths = {"customer"})
    Optional<CurrentAccountEntity> findByAccountNumber(String accountNumber);
    @Query("SELECT a FROM CurrentAccountEntity a WHERE a.expireDate < :date " + "AND a.currentAccountStatus != :canceled " + "AND a.currentAccountStatus != :expired")
    List<CurrentAccountEntity> findExpiredAccounts(@Param("date") LocalDate date, @Param("canceled") CurrentAccountStatus canceled, @Param("expired") CurrentAccountStatus expired);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT a FROM CurrentAccountEntity a WHERE a.accountNumber = :accountNumber")
    Optional<CurrentAccountEntity> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM CurrentAccountEntity a " + "WHERE a.customer.id = :customerId " + "AND a.currentAccountStatus = :status")
    List<CurrentAccountEntity> findByCustomerIdAndCurrentAccountStatusForUpdate(@Param("customerId") Long customerId, @Param("status") CurrentAccountStatus status);
}
