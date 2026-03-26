package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.model.entity.CardOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardOrderRepository extends JpaRepository<CardOrderEntity, Long> {
}
