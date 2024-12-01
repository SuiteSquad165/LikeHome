package org.suitesquad.likehome.rest;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * This class contains the response types for the REST API.
 * All these records are to be serialized to JSON. They are not used in the database.
 * <p>
 * There is a sample of each record in the class definition, for testing purposes.
 */
public class RestTypes {
    public record SignUpInfo(String email, String firstName, String lastName) {}

    public record HotelInfo(String id, String name, String description, double rating,
                            int numberOfReviews, String city, List<String> imageUrls, List<String> roomsIds) {}

    public record RoomInfo(String id, String name, int baths, int beds, int guests, int bedrooms, String description, double pricePerNight, List<String> amenities, List<String> imageUrls) {}

    public record ReservationInfo(String id, String hotelId, String userId, String roomId, Date checkInDate,
                                  Date checkOutDate) {}

    public record ReviewInfo(String id, String userId, String contents, double rating, Date reviewDate) {}

    public record ChatMessage(Sender sender, String content) {
        enum Sender {
            USER, ASSISTANT
        }
    }
}
