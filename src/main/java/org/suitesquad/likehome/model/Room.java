package org.suitesquad.likehome.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("rooms")
@Getter
@Setter
public class Room {
    @Id
    @Indexed(unique = true)
    private String id;

    private String hotelId;
    private String roomType;
    private double price;   // price per night
    private int availability;
    private List<String> features;
    private List<String> imageUrls;
}