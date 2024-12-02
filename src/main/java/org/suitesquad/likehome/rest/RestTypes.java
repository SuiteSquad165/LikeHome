package org.suitesquad.likehome.rest;

import org.suitesquad.likehome.model.Reservation;

import java.util.Date;
import java.util.List;

/**
 * This class contains the response types for the REST API.
 * All these records are to be serialized to JSON. They are not used in the database.
 */
public class RestTypes {
    public record SignUpInfo(String email, String firstName, String lastName) {}

    public record HotelInfo(String id, String name, String description, double rating,
                            int numberOfReviews, String city, List<String> imageUrls, List<String> roomsIds) {}

    // just using model.Room in API to avoid duplicating the fields

    public record ReservationRequest(String roomId, int nights, Reservation.Payment payment, Date checkIn,
                                     Date checkOut) {}

    public record ReservationUpdate(Date checkIn, Date checkOut) {}

    public record ReservationInfo(String id, String userId, String hotelId, String roomId, Date checkIn,
                                  Date checkOut, double totalPrice, Reservation.Payment payment,
                                  boolean cancelled) {}

    public record ReviewUpdate(String contents, double rating) {}

    public record ReviewInfo(String id, String firstName, String contents, double rating, Date reviewDate) {}

    public record ChatMessage(Sender sender, String content) {
        enum Sender {
            USER, ASSISTANT
        }
    }
}
