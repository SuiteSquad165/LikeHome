package org.suitesquad.likehome.model;
import java.time.LocalDateTime;

public class BookingHistory {
    private String reservationId;  // Reference to reservation (ObjectId in string form)
    private String hotelId;  // Reference to hotel (ObjectId in string form)
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private double price;
    private String status;
}