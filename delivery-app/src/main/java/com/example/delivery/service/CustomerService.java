package com.example.delivery.service;

import com.example.delivery.exception.NotFoundException;
import com.example.delivery.model.Customer;
import com.example.delivery.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(String name, String email, String phone, String address) {
        return customerRepository.save(new Customer(name, email, phone, address));
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer " + customerId + " not found"));
    }
}
