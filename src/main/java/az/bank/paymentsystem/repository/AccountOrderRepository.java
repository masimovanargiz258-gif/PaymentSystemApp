package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.model.entity.AccountOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountOrderRepository extends JpaRepository<AccountOrderEntity, Long> {
}
