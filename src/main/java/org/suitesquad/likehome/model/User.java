package org.suitesquad.likehome.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Document("users")
public class User {

    @Id
    @Indexed(unique = true)
    private String id;
    @Setter
    @Getter
    private String userId;
    @Setter
    @Getter
    private String email;
}