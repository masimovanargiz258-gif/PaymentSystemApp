package az.bank.paymentsystem.util;

import az.bank.paymentsystem.exception.ForbiddenOperationException;
import az.bank.paymentsystem.model.entity.UserEntity;
import az.bank.paymentsystem.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final MessageService messageService;

    public UserEntity getCurrentUser() {
        return (UserEntity) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public Long getCurrentCustomerId() {
        return getCurrentUser().getCustomer().getId();
    }
    public boolean isNotAdmin() {
        return getCurrentUser().getAuthorities()
                .stream()
                .noneMatch(a -> a.getAuthority()
                        .equals("ROLE_ADMIN"));
    }

    public void checkOwnership(Long resourceCustomerId) {
        if (isNotAdmin()) {
            Long currentCustomerId = getCurrentCustomerId();
            if (!resourceCustomerId.equals(currentCustomerId)) {
                throw new ForbiddenOperationException(
                        messageService.getMessage("access.denied"));
            }
        }
    }
}
