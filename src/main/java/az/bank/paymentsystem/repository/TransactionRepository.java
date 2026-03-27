package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.model.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    @Query("SELECT t FROM TransactionEntity t " + "WHERE t.fromAccountNumber = :accountNumber " + "AND t.paymentSourceType = :sourceType " + "ORDER BY t.transactionDate DESC LIMIT 100")
    List<TransactionEntity> findTop100ByAccountNumber(@Param("accountNumber") String accountNumber, @Param("sourceType") PaymentSourceType sourceType);
    @Query("SELECT t FROM TransactionEntity t " + "WHERE t.fromAccountNumber = :accountNumber " + "ORDER BY t.transactionDate DESC LIMIT 100")
    List<TransactionEntity> findTop100ByAccountNumberOnly(@Param("accountNumber") String accountNumber);
}