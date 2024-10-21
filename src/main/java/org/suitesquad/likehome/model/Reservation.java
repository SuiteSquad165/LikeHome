package org.suitesquad.likehome.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document("reservations")
public class Reservation {
    @Id
    @Indexed(unique = true)
    private String id;

    @Getter
    //@Indexed
    private String userId;

    @Getter
    //@Indexed
    private String hotelId;

    private List<RoomBooked> roomsBooked;
    private Date checkIn;
    private Date checkOut;
    private double totalPrice;
    private String status;
    private Date bookingDate;
    private Payment payment;
    private CancellationPolicy cancellationPolicy;

    public static class RoomBooked {
        private String roomType;
        private double price;
        private int quantity;
    }

    public static class Payment {
        private String paymentMethod;
        private String paymentStatus;
        private Date paymentDate;
    }

    public static class CancellationPolicy {
        private boolean allowed;
        private double penaltyFee;
        private Date lastCancellationDate;
    }
}