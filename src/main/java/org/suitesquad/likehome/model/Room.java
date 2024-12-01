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

    private String name;    // Room Type or title (Basic Suite, Oceanside Deluxe, etc.)
    private int baths;
    private int beds;
    private int guests;
    private String description;
    private double pricePerNight;
    private int availability;
    private List<String> imageUrls;
    private List<String> amenities;
}
