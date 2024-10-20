package org.suitesquad.likehome.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.time.LocalDate;
import org.suitesquad.likehome.model.Address;
import org.suitesquad.likehome.model.BookingHistory;


import java.time.LocalDate;
import java.util.List;

@Document("users")
public class User {

    @Id
    @Indexed(unique = true)
    private String id;

    @Indexed(unique = true)
    private String userId;  // Globally unique user identifier (UUID)

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    @Getter
    private String email;

    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String passwordHash;
    private String salt;
    private LocalDateTime accountCreated;
    private LocalDateTime lastLogin;
    private boolean isVerified;

    private Address address;
    private List<BookingHistory> bookingHistory;
}