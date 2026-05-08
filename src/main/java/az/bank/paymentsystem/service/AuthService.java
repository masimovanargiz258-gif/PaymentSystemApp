package az.bank.paymentsystem.service;

import az.bank.paymentsystem.enums.Role;
import az.bank.paymentsystem.exception.InvalidRegistrationTokenException;
import az.bank.paymentsystem.exception.UserNotFoundException;
import az.bank.paymentsystem.exception.UsernameAlreadyExistsException;
import az.bank.paymentsystem.model.AdminRequest;
import az.bank.paymentsystem.model.AuthResponse;
import az.bank.paymentsystem.model.LoginRequest;
import az.bank.paymentsystem.model.RegisterRequest;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.model.entity.UserEntity;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MessageService messageService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    messageService.getMessage("username.already.exists"));
        }

        CustomerEntity customer = customerRepository.findByRegistrationToken(request.getRegistrationToken())
                .orElseThrow(() -> new InvalidRegistrationTokenException(messageService.getMessage("registration.token.invalid")));

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCustomer(customer);
        userRepository.save(user);

        customer.setRegistrationToken(null);
        customerRepository.save(customer);

        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword())
        );
        UserEntity user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new UserNotFoundException(messageService.getMessage("user.not.found")));
        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse createAdmin(AdminRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(messageService.getMessage("username.already.exists"));
        }
        UserEntity admin = new UserEntity();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        return new AuthResponse(jwtService.generateToken(admin));
    }
}