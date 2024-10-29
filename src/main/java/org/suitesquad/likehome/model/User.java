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

import java.util.List;

@Document("users")
public class User {

    @Id
    @Indexed(unique = true)
    private String id;

    @Indexed(unique = true)
    @Getter
    private String email;

    private String firstName;
    private String lastName;

    private List<BookingHistory> bookingHistory;
}