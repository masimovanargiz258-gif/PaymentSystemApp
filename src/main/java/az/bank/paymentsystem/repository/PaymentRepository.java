package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.model.entity.PaymentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    @Query("SELECT p FROM PaymentEntity p WHERE p.customer.id = :customerId AND p.createdAt >= :startOfToday")
    List<PaymentEntity> findAllByCustomerIdCreatedAtAfter(@Param("customerId") Long customerId, @Param("startOfToday") LocalDateTime startOfToday);
    @EntityGraph(attributePaths = {"customer","customer.cards", "customer.currentAccounts"})
    List<PaymentEntity> findAllByPaymentStatus(PaymentStatus paymentStatus);


}
