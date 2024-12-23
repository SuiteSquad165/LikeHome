package org.suitesquad.likehome.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.suitesquad.likehome.model.Reservation;
import org.suitesquad.likehome.model.Review;
import org.suitesquad.likehome.model.Room;
import org.suitesquad.likehome.model.User;
import org.suitesquad.likehome.rest.RestTypes.*;
import org.suitesquad.likehome.service.*;

import java.util.*;

/**
 * This class handles all authenticated requests.
 * Authenticated means the user has a valid JWT.
 * Any user that is authenticated can access these endpoints.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authenticated", description = "Any user that is authenticated (valid JWT) can access these endpoints.")
public class AuthenticatedController {

    @Autowired private UserService userService;
    @Autowired private ReservationService reservationService;
    @Autowired private HotelService hotelService;
    @Autowired private ReviewService reviewService;
    @Autowired private RoomService roomService;

    /**
     * Creates a user in the database with the email and name from the SignUpInfo object
     * and the user ID retrieved from the token.
     */
    @PostMapping(path = "/signup")
    public User signUp(@RequestBody SignUpInfo info, JwtAuthenticationToken token) {
        if (userService.findById(getUserID(token)).isPresent()) {
            throw new RuntimeException("User already exists in database!");
        }
        var user = new User();
        user.setId(getUserID(token));
        user.setEmail(info.email());
        user.setFirstName(info.firstName());
        user.setLastName(info.lastName());
        userService.addUserData(user);
        return user;
    }

    /**
     * Called when a user has signed in.
     * Checks database for user data consistency.
     */
    @PostMapping(path = "/signin")
    public User signedIn(JwtAuthenticationToken token) {
        Optional<User> user = userService.findById(getUserID(token));
        if (user.isPresent()) {
            return user.get();
        }
        return null;
    }

    /**
     * Get the reservations for this user.
     */
    @GetMapping(path = "/reservations")
    public List<ReservationInfo> getReservations(JwtAuthenticationToken token) {
        return reservationService.findByUserId(getUserID(token)).stream()
                .map(reservation -> new ReservationInfo(
                        reservation.getId(),
                        reservation.getUserId(),
                        reservation.getHotelId(),
                        reservation.getRoomId(),
                        reservation.getCheckIn(),
                        reservation.getCheckOut(),
                        reservation.getTotalPrice(),
                        reservation.getPayment(),
                        reservation.getCancellationDate() != null))
                .toList();
    }

    /**
     * Create a reservation for this user.
     * <p>
     * TODO: verify payment and that the room is available
     *
     * @return the created reservation's ID
     */
    @PostMapping(path = "/reservations")
    public String createReservation(JwtAuthenticationToken token, @RequestBody ReservationRequest reservationInfo) {
        if (reservationInfo.checkIn().after(reservationInfo.checkOut())) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }
        if (reservationInfo.checkIn().before(new Date())) {
            throw new IllegalArgumentException("Check-in date must be in the future");
        }
        User user = userService.findById(getUserID(token))
                .orElseThrow(() -> new IllegalStateException("User not found in database"));
        reservationService.findByUserId(user.getId()).stream()
                .filter(reservation -> reservation.getCheckIn().before(reservationInfo.checkOut()) &&
                                       reservation.getCheckOut().after(reservationInfo.checkIn()))
                .findAny().ifPresent(reservation -> {
                    throw new IllegalArgumentException("User already has a reservation for this time period");
                });
        Room room = roomService.findById(reservationInfo.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Room '" + reservationInfo.roomId() + "' not found"));


        var reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setHotelId(room.getHotelId());
        reservation.setRoomId(reservationInfo.roomId());
        reservation.setCheckIn(reservationInfo.checkIn());
        reservation.setCheckOut(reservationInfo.checkOut());
        reservation.setTotalPrice(room.calculateTotalPrice(reservationInfo.nights()));
        reservation.setBookingDate(new Date());
        reservation.setPayment(reservationInfo.payment());

        int userPointsAfterReservation = user.getRewardPoints() + reservation.calculatePointsGainedOrLost();
        if (userPointsAfterReservation < 0) {
            throw new RuntimeException("User does not have enough points to make this reservation");
        }
        String id = reservationService.addReservationData(reservation).getId();
        userService.updateUserPoints(user.getId(), userPointsAfterReservation);
        return id;
    }

    /**
     * Get a specific reservation for this user by ID.
     */
    @GetMapping(path = "/reservations/{reservationId}")
    public ReservationInfo getReservationById(JwtAuthenticationToken token, @PathVariable String reservationId) {
        Reservation reservation = reservationService.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation '" + reservationId + "' does not exist!"));

        if (!reservation.getUserId().equals(getUserID(token))) {
            throw new AccessDeniedException("Reservation '" + reservationId + "' does not belong to this user!");
        }

        return new ReservationInfo(
                reservation.getId(),
                reservation.getHotelId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getTotalPrice(),
                reservation.getPayment(),
                reservation.getCancellationDate() != null);
    }

    /**
     * Cancel a reservation for this user.
     *
     * @return the penalty fee for cancelling the reservation, as a double
     */
    @DeleteMapping(path = "/reservations/{reservationId}")
    public double cancelReservation(JwtAuthenticationToken token, @PathVariable String reservationId) {
        Reservation reservation = reservationService.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation '" + reservationId + "' does not exist!"));
        User user = userService.findById(getUserID(token))
                .orElseThrow(() -> new RuntimeException("User not found in database"));
        if (!reservation.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Reservation '" + reservationId + "' does not belong to this user!");
        }
        Room room = roomService.findById(reservation.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room '" + reservation.getRoomId() + "' not found"));
        if (!room.getCancellationPolicy().isAllowed()) {
            throw new RuntimeException("Cancellation not allowed for this reservation!");
        }

        reservation.setCancellationDate(new Date());
        reservation.getPayment().setPaymentStatus("Refunded");
        reservationService.save(reservation);
        userService.updateUserPoints(user.getId(), user.getRewardPoints() - reservation.calculatePointsGainedOrLost());

        return room.getCancellationPolicy().getPenaltyFee();
    }

    /**
     * Update the check in or check out date for a reservation.
     */
    @PatchMapping(path = "/reservations/{reservationId}")
    public void updateReservation(JwtAuthenticationToken token, @PathVariable String reservationId,
                                  @RequestBody ReservationUpdate update) {
        if (update.checkIn().after(update.checkOut())) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }
        if (update.checkIn().before(new Date())) {
            throw new IllegalArgumentException("Check-in date must be in the future");
        }
        Reservation reservation = reservationService.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation '" + reservationId + "' does not exist!"));

        User user = userService.findById(getUserID(token))
                .orElseThrow(() -> new IllegalStateException("User not found in database"));
        if (!reservation.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Reservation '" + reservationId + "' does not belong to this user!");
        }

        reservationService.findByUserId(user.getId()).stream()
                .filter(res -> res.getCheckIn().before(update.checkOut()) &&
                               res.getCheckOut().after(update.checkIn()))
                .findAny().ifPresent(res -> {
                    throw new IllegalArgumentException("User already has a reservation for this time period");
                });

        reservation.setCheckIn(update.checkIn());
        reservation.setCheckOut(update.checkOut());

        reservationService.save(reservation);
    }

    /**
     * Add a review for a hotel.
     * Updates an existing review if the user already reviewed the hotel.
     * Error if user has not stayed at the hotel.
     */
    @PostMapping(path = "/hotels/{hotelId}/reviews")
    public void reviewHotel(JwtAuthenticationToken token, @PathVariable String hotelId, @RequestBody ReviewUpdate reviewInfo) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        if (reservationService.findByUserIdAndHotelId(getUserID(token), hotelId).isEmpty()) {
            throw new RuntimeException("User has not stayed at hotel '" + hotelId + "'");
        }
        if (reviewService.findByHotelIdAndUserId(hotelId, getUserID(token)) != null) {
            throw new RuntimeException("User '" + getUserID(token) + "' already left a review for hotel '" + hotelId + "'");
        }

        var review = new Review();
        review.setHotelId(hotelId);
        review.setUserId(getUserID(token));
        review.setContents(reviewInfo.contents());
        review.setRating(reviewInfo.rating());
        review.setReviewDate(new Date());

        reviewService.addReviewData(review);
    }

    @PatchMapping("/hotels/{hotelId}/reviews")
    public void updateReviewHotel(JwtAuthenticationToken token, @PathVariable String hotelId, @RequestBody ReviewUpdate reviewInfo) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        Review review = reviewService.findByHotelIdAndUserId(hotelId, getUserID(token));
        if (review == null) {
            throw new RuntimeException("User has not left a review for hotel '" + hotelId + "'");
        }

        review.setHotelId(hotelId);
        review.setUserId(getUserID(token));
        review.setRating(reviewInfo.rating());
        review.setContents(reviewInfo.contents());
        review.setReviewDate(new Date());

        reviewService.updateReviewData(review);
    }

    @DeleteMapping("/hotels/{hotelId}/reviews")
    public void deleteHotelReview(JwtAuthenticationToken token, @PathVariable String hotelId) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new NoSuchElementException("Hotel '" + hotelId + "' not found"));

        Review review = reviewService.findByHotelIdAndUserId(hotelId, getUserID(token));
        if (review == null) {
            throw new RuntimeException("User has not left a review for hotel '" + hotelId + "'");
        }

        reviewService.deleteById(review.getId());
    }

    /**
     * Respond to a user's chat message with a response from an AI assistant.
     *
     * @param messageHistory the chat history between the user and the chatbot with the latest message at the end
     */
    @PostMapping(path = "/chat")
    public String chat(JwtAuthenticationToken token, @RequestBody List<ChatMessage> messageHistory) {
        return "Hello! How can I help you today?";
    }

    /**
     * @return the user id and the token passed in
     */
    @GetMapping(path = "/test")
    public Map<String, String> test(JwtAuthenticationToken token) {
        return Map.of("user id", getUserID(token), "token", token.getToken().getTokenValue());
    }

    /**
     * Converts the JWT Auth Token to a user id.
     *
     * @param token JWT Token
     * @return User ID
     */
    private static String getUserID(JwtAuthenticationToken token) {
        return token.getName();
    }
}