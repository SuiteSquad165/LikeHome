package org.suitesquad.likehome.rest;

import java.time.LocalDate;

/**
 * This class contains the response types for the REST API.
 * All these records are to be serialized to JSON. They are not used in the database.
 * <p>
 * There is a sample of each record in the class definition, for testing purposes.
 */
public class RestTypes {
    public record SignUpInfo(String email, String firstName, String lastName) {}

    public record Room(String id, String name, String description, double rating,
                       int numberOfReviews, String city, int pricePerNight, String imageUrl) {
        public static final Room[] sample = new Room[] {
                new Room("1", "Oceanview Resort", "A beautiful beachfront resort with stunning ocean views.", 4.5, 250, "Miami, FL", 200, "https://hoteldel.com/wp-content/uploads/2021/01/hotel-del-coronado-views-suite-K1TOS1-K1TOJ1-1600x900-1.jpg"),
                new Room("2", "Mountain Escape", "Cozy cabins nestled in the mountains, perfect for a getaway.", 4.8, 180, "Aspen, CO", 150, "https://plus.unsplash.com/premium_photo-1661964071015-d97428970584?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aG90ZWx8ZW58MHx8MHx8fDA%3D"),
                new Room("3", "City Lights Hotel", "Modern rooms located in the heart of the city.", 4.2, 320, "San Francisco, CA", 250, "https://images.bubbleup.com/width1920/quality35/mville2017/1-brand/1-margaritaville.com/gallery-media/220803-compasshotel-medford-pool-73868-1677873697-78625-1694019828.jpg"),
                new Room("4", "Desert Oasis", "A unique experience in the desert with luxury amenities.", 4.6, 100, "Las Vegas, NV", 300, "https://t3.ftcdn.net/jpg/00/29/13/38/360_F_29133877_bfA2n7cWV53fto2BomyZ6pyRujJTBwjd.jpg"),
                new Room("5", "Countryside Inn", "Charming inn located in the peaceful countryside.", 4.3, 75, "San Jose, CA", 120, "https://cdn.prod.website-files.com/5c6d6c45eaa55f57c6367749/65046bf150d1abb7e5911702_x-65046bcfdc4f0.webp")
        };
    }

    public record Reservation(String id, String roomId, String userId, LocalDate checkInDate, LocalDate checkOutDate, int guests) {
        public static final Reservation[] sample = new Reservation[] {
                new Reservation("a1", "1", "user1", LocalDate.of(2025, 11, 30), LocalDate.of(2023, 12, 5), 2),
                new Reservation("b2", "2", "user1", LocalDate.of(2025, 12, 10), LocalDate.of(2023, 12, 15), 4),
                new Reservation("c3", "3", "user1", LocalDate.of(2025, 12, 20), LocalDate.of(2023, 12, 25), 1)
        };
    }

    public record ChatMessage(Sender sender, String content) {
         enum Sender {
            USER, ASSISTANT
        }
    }

}
