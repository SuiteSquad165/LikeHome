package org.suitesquad.likehome.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document("hotels")
@Getter
@Setter
public class Hotel {
    @Id
    @Indexed(unique = true)
    private String id;

    @Indexed
    private String name;

    private String description;
    private double rating;
    private Location location;
    private ContactInfo contactInfo;
    private List<Room> rooms;
    private List<String> amenities;
    private List<String> imageUrls;

    @Getter
    @Setter
    public static class Room {
        private String roomType;
        private double price;   // price per night
        private int availability;
        private List<String> features;
        private String imageUrl;
    }

    @Getter
    @Setter
    static public class ContactInfo {
        private String phone;
        private String email;
        private String website;
    }

    @Getter
    @Setter
    public static class Location {
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private Coordinates coordinates;

        @Getter
        @Setter
        public static class Coordinates {
            private double latitude;
            private double longitude;
        }
    }
}
