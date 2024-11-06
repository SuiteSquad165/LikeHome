package org.suitesquad.likehome.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("reviews")
@Getter
@Setter
public class Review {
    @Id
    private String id;  // Review ID

    private String userId;

    @Indexed
    private String hotelId;
    private String contents;
    private double rating;
    private LocalDateTime reviewDate;
}
