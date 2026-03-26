package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.model.entity.CardEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long> {
    @EntityGraph(attributePaths = {"customer"})
    List<CardEntity> findByCustomerId(Long customerId);
    boolean existsByPan(String pan);
    long countByCustomerId(Long customerId);
    @Query("SELECT c FROM CardEntity c WHERE c.expireDate < :date " + "AND c.cardStatus != :canceled " + "AND c.cardStatus != :expire")
    List<CardEntity> findExpiredCards(@Param("date") LocalDate date, @Param("canceled") CardStatus canceled, @Param("expire") CardStatus expire);
    @EntityGraph(attributePaths = {"customer"})
    Optional<CardEntity> findByPan(String pan);
    @EntityGraph(attributePaths = {"customer"})
    Optional<CardEntity> findWithCustomerById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT c FROM CardEntity c WHERE c.pan = :pan")
    Optional<CardEntity> findByPanForUpdate(@Param("pan") String pan);
}
