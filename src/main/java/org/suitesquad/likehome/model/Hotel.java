package org.suitesquad.likehome.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;


@Document("hotels")
public class Hotel {
    @Id
    @Indexed(unique = true)
    private String id;

    @Getter
    @Indexed
    private String name;

    private String description;
    private double rating;
    private Location location;
    private ContactInfo contactInfo;
    private List<Room> rooms;
    private List<String> amenities;
    private List<Review> reviews;
}
