package org.suitesquad.likehome.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("reviews")
@Getter
@Setter
public class Review {
    @Id
    private String id;  // Review ID

    private String hotelId;
    private String userId;
    private String contents;
    private double rating;
    private Date reviewDate;
}
