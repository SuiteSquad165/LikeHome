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
    private List<String> roomIds;
    private List<String> amenities;
    private List<Review> reviews;
    private List<String> imageUrls;

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
