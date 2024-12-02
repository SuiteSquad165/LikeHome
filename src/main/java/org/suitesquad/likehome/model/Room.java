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
    private int bedrooms;
    private String description;
    private double pricePerNight;
    private double cleaningFee;
    private double serviceFee;
    /**
     * Tax rate as a decimal (0.1 for 10%).
     * To calculate total: total = ((pricePerNight * nights) + cleaningFee + serviceFee) * (1 + taxRate)
     */
    private double taxRate;
    private int availability;
    private List<String> imageUrls;
    private List<String> amenities;
    private CancellationPolicy cancellationPolicy;

    @Getter
    @Setter
    public static class CancellationPolicy {
        private boolean allowed;
        private double penaltyFee;
    }

    public double calculateTotalPrice(int nights) {
        return ((pricePerNight * nights) + cleaningFee + serviceFee) * (1 + taxRate);
    }
}
