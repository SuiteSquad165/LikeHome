package org.suitesquad.likehome.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("reservations")
@Getter
@Setter
public class Reservation {
    @Id
    @Indexed(unique = true)
    private String id;

    @Indexed
    private String userId;

    //@Indexed
    private String hotelId;

    //@Indexed
    private String roomId;

    private Date checkIn;
    private Date checkOut;
    private double totalPrice;
    private Date bookingDate;
    private Payment payment;
    /**
     * null if reservation is not cancelled
     */
    private Date cancellationDate;

    @Getter
    @Setter
    public static class Payment {
        private double pointsUsed;
        private String paymentMethod;
        private String paymentStatus;
    }

    public int calculatePointsEarned() {
        return (int) (totalPrice - (payment.pointsUsed / 100));
    }
}