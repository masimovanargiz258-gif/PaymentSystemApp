package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.CustomerRequest;
import az.bank.paymentsystem.model.CustomerResponse;
import az.bank.paymentsystem.model.CustomerUpdateRequest;
import az.bank.paymentsystem.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@Tag( name = "Customer", description = "Customer API")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    @GetMapping("/getById/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get customer by id")
    public CustomerResponse getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @GetMapping("/getAll")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all customers")
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create customer")
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
        return customerService.createCustomer(customerRequest);
    }
    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = " Full Update customer")
    public CustomerResponse updateFullCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest customerRequest) {
        return customerService.updateFullCustomer(id, customerRequest);
    }
    @PatchMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = " Half Update customer")
    public CustomerResponse updateHalfCustomer(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest customerRequest){
        return customerService.updateHalfCustomer(id,customerRequest);
    }
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete customer")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }

    @PatchMapping("/block/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Block customer")
    public void blockCustomer(@PathVariable Long id) {
        customerService.blockCustomer(id);
    }

    @PatchMapping("/unblock/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Unblock customer")
    public void unblockCustomer(@PathVariable Long id) {
        customerService.unblockCustomer(id);
    }
}
