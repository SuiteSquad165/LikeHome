package org.suitesquad.likehome.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

import java.util.List;

@Document("users")
@Getter
@Setter
public class User {
    @Id
    @Indexed(unique = true)
    private String id;

    @Indexed(unique = true)
    private String email;

    private String firstName;
    private String lastName;

    private int rewardPoints;
}