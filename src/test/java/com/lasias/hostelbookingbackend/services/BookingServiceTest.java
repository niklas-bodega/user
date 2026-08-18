package com.lasias.hostelbookingbackend.services;

import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import jakarta.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingServiceTest {
    @Autowired
    private AppUserRepository appUserRepository;
    @Inject
    private AppUserService appUserService;
    @Autowired
    private BookingRepository bookingRepository;
    @Inject
    private BookingService bookingService;


}