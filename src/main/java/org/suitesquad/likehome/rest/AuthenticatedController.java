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
        return userService.findById(getUserID(token))
                .orElseThrow(() -> new NoSuchElementException("User not found in database"));
    }

    /**
     * Get the reservations for this user.
     */
    @GetMapping(path = "/reservations")
    public List<ReservationInfo> getReservations(JwtAuthenticationToken token) {
        return reservationService.findByUserId(getUserID(token)).stream()
                .map(reservation -> new ReservationInfo(
                        reservation.getId(),
                        reservation.getHotelId(),
                        reservation.getUserId(),
                        reservation.getRoomId(),
                        reservation.getCheckIn(),
                        reservation.getCheckOut()))
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
        User user = userService.findById(getUserID(token))
                .orElseThrow(() -> new RuntimeException("User not found in database"));
        reservationService.findByUserId(user.getId()).stream()
                .filter(reservation -> reservation.getCheckIn().before(reservationInfo.checkOutDate()) &&
                                       reservation.getCheckOut().after(reservationInfo.checkInDate()))
                .findAny().ifPresent(reservation -> {
                    throw new RuntimeException("User already has a reservation for this time period");
                });
        Room room = roomService.findById(reservationInfo.roomId())
                .orElseThrow(() -> new RuntimeException("Room '" + reservationInfo.roomId() + "' not found"));

        var reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setHotelId(room.getHotelId());
        reservation.setRoomId(reservationInfo.roomId());
        reservation.setCheckIn(reservationInfo.checkInDate());
        reservation.setCheckOut(reservationInfo.checkOutDate());
        reservation.setTotalPrice(room.calculateTotalPrice(reservationInfo.nights()));
        reservation.setBookingDate(new Date());
        reservation.setPayment(reservationInfo.payment());

        String id = reservationService.addReservationData(reservation).getId();
        userService.updateUserPoints(user.getId(), user.getRewardPoints() + reservation.calculatePointsGainedOrLost());
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
                reservation.getCheckOut());
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
     * Add a review for a hotel.
     * Updates an existing review if the user already reviewed the hotel.
     * Error if user has not stayed at the hotel.
     */
    @PostMapping(path = "/hotels/{hotelId}/reviews")
    public void reviewHotel(JwtAuthenticationToken token, @PathVariable String hotelId, @RequestBody ReviewInfo reviewInfo) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        if (reservationService.findByUserIdAndHotelId(getUserID(token), hotelId).isEmpty()) {
            throw new RuntimeException("User has not stayed at hotel '" + hotelId + "'");
        }

        if(reviewService.findByHotelIdAndUserId(hotelId, getUserID(token)) != null){
            throw new RuntimeException("User '" + getUserID(token) + "' already left a review for hotel '" + hotelId + "'");
        }

        var review = new Review();
        if (reviewInfo.id() != null) {
            Optional<Review> oldReview = reviewService.findById(reviewInfo.id());
            if (oldReview.isPresent() && oldReview.get().getUserId().equals(getUserID(token))) {
                review.setId(reviewInfo.id());
            }
        }

        review.setHotelId(hotelId);
        review.setUserId(getUserID(token));
        review.setRating(reviewInfo.rating());
        review.setContents(reviewInfo.contents());
        review.setReviewDate(reviewInfo.reviewDate());

        reviewService.addReviewData(review);
    }

    @PatchMapping("/hotels/{hotelId}/reviews")
    public void updateReviewHotel(JwtAuthenticationToken token, @PathVariable String hotelId, @RequestBody ReviewInfo reviewInfo) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        if (reservationService.findByUserIdAndHotelId(getUserID(token), hotelId).isEmpty()) {
            throw new RuntimeException("User '" + getUserID(token) + "' has not stayed at hotel '" + hotelId + "'");
        }

        Review review = reviewService.findByHotelIdAndUserId(hotelId, getUserID(token));
        if(review == null){
            throw new RuntimeException("User '" + getUserID(token) + "' has not left a review for hotel '" + hotelId + "'");
        }

        review.setHotelId(hotelId);
        review.setUserId(getUserID(token));
        review.setRating(reviewInfo.rating());
        review.setContents(reviewInfo.contents());
        review.setReviewDate(reviewInfo.reviewDate());

        reviewService.updateReviewData(review);
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