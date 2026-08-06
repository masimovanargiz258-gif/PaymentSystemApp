package az.bank.paymentsystem.scheduler;

import az.bank.paymentsystem.service.CardService;
import az.bank.paymentsystem.service.CurrentAccountService;
import az.bank.paymentsystem.service.TransactionService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PaymentScheduler {
    private final TransactionService transactionService;
    private final CardService cardService;
    private final CurrentAccountService currentAccountService;

    @Scheduled(cron = "${scheduler.payment.cron}")
    @SchedulerLock(name = "paymentSchedulerLock", lockAtMostFor = "PT1H")
    public void processPayments() {
        transactionService.processAllPendingPayments();
    }
    @Scheduled(cron = "${scheduler.card.expire.cron}")
    @SchedulerLock(name = "expiredCardSchedulerLock", lockAtMostFor = "PT1H")
    public void checkExpiredCards() {
        cardService.expireCards();
    }

    @Scheduled(cron = "${scheduler.account.expire.cron}")
    @SchedulerLock(name = "expiredAccountSchedulerLock", lockAtMostFor = "PT1H")
    public void checkExpiredAccounts() {
        currentAccountService.expireAccounts();
    }
}