package org.suitesquad.likehome.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.suitesquad.likehome.model.Hotel;
import org.suitesquad.likehome.model.Reservation;
import org.suitesquad.likehome.model.Review;
import org.suitesquad.likehome.model.User;
import org.suitesquad.likehome.rest.RestTypes.ReservationInfo;
import org.suitesquad.likehome.rest.RestTypes.ChatMessage;
import org.suitesquad.likehome.rest.RestTypes.ReviewInfo;
import org.suitesquad.likehome.rest.RestTypes.SignUpInfo;
import org.suitesquad.likehome.service.HotelService;
import org.suitesquad.likehome.service.ReservationService;
import org.suitesquad.likehome.service.ReviewService;
import org.suitesquad.likehome.service.UserService;

import java.time.ZoneId;
import java.util.*;

/**
 * This class handles all authenticated requests.
 * Authenticated means the user has a valid JWT token.
 * Any user that is authenticated can access these endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthenticatedController {

    @Autowired private UserService userService;
    @Autowired private ReservationService reservationService;
    @Autowired private HotelService hotelService;
    @Autowired private ReviewService reviewService;

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
        return userService.findById(getUserID(token)).orElseThrow(()-> new NoSuchElementException("User not found in database"));
    }

    /**
     * Get the reservations for this user.
     */
    @GetMapping(path = "/reservations")
    public List<ReservationInfo> getReservations(JwtAuthenticationToken token) {
        List<ReservationInfo> reservations = new ArrayList<>();
        System.out.println(getUserID(token));
        List<Reservation> reservationList = reservationService.findByUserId(getUserID(token));
        for(Reservation reservation : reservationList) {
            reservations.add(new ReservationInfo(
                    reservation.getId(),
                    reservation.getHotelId(),
                    reservation.getUserId(),
                    reservation.getRoomId(),
                    reservation.getCheckIn(),
                    reservation.getCheckOut()));
        }

        return reservations;
    }

    /**
     * Get a specific reservation for this user by ID.
     */
    @GetMapping(path = "/reservations/{reservationId}")
    public ReservationInfo getReservationById(JwtAuthenticationToken token, @PathVariable String reservationId) {
        ReservationInfo reservationInfo;

        Reservation reservation = reservationService.findById(reservationId).isPresent() ? reservationService.findById(reservationId).get() : null;
        if(reservation == null) {
            throw new RuntimeException("Reservation '" + reservationId + "' does not exist!");
        }

        reservationInfo = new ReservationInfo(
                reservation.getId(),
                reservation.getHotelId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getCheckIn(),
                reservation.getCheckOut());

        return reservationInfo;
    }

    @GetMapping("/reservations/user/{userId}")
    public List<ReservationInfo> getReservationsByUserId(JwtAuthenticationToken token, @PathVariable String userId) {
        List<ReservationInfo> reservationInfos = new ArrayList<>();

        if (userService.findById(userId).isEmpty()) {
            throw new RuntimeException("User '" + userId + "' does not exist!");
        }

        List<Reservation> reservations = reservationService.findByUserId(userId);

        for(Reservation reservation : reservations) {
            reservationInfos.add(new ReservationInfo(
                    reservation.getId(),
                    reservation.getHotelId(),
                    reservation.getUserId(),
                    reservation.getRoomId(),
                    reservation.getCheckIn(),
                    reservation.getCheckOut()
            ));
        }

        return reservationInfos;
    }

    /**
     * Create a reservation for this user.
     * The reservation.id field and reservation.userID fields are ignored.
     * Instead, the id is generated and assigned, and the user ID is retrieved from the token.
     */
    @PostMapping(path = "/reservations")
    public void createReservation(JwtAuthenticationToken token, @RequestBody ReservationInfo reservationInfo) {
        Reservation reservationDetails = new Reservation();

        reservationDetails.setHotelId(reservationInfo.hotelId());
        reservationDetails.setUserId(reservationInfo.userId());
        reservationDetails.setRoomId(reservationInfo.roomId());
        reservationDetails.setCheckIn(reservationInfo.checkInDate());
        reservationDetails.setCheckOut(reservationInfo.checkOutDate());

        reservationService.addReservationData(reservationDetails);

        Optional<User> user_optional = userService.findById(reservationInfo.userId());
        if(user_optional.isPresent()) {
            User user = user_optional.get();
            userService.updateUserPoints(reservationInfo.userId(), user.getPoints() + 10);
        }
    }

    @PostMapping("/hotels/{hotelId}/reviews")
    public void createReview(JwtAuthenticationToken token, @PathVariable String hotelId, @RequestBody ReviewInfo reviewInfo) {
        Review review = new Review();

        Hotel hotel = hotelService.findById(hotelId);
        if(hotel == null) {
            throw new RuntimeException("Hotel '" + hotelId + "' not found");
        }

        review.setHotelId(hotelId);
        review.setUserId(reviewInfo.userId());
        review.setRating(reviewInfo.rating());
        review.setContents(reviewInfo.contents());
        review.setReviewDate(reviewInfo.reviewDate());

        reviewService.addReviewData(review);

        hotelService.updateReviewCount(hotel.getId(), hotel.getReviewCount() + 1);
    }

    /**
     * Add a review for a hotel. Updates an existing review if the user already reviewed the hotel.
     * Error if user has not stayed at the hotel.
     */
    @PutMapping(path = "/review/{hotelId}")
    public void reviewHotel(JwtAuthenticationToken token, @PathVariable String hotelId, @RequestBody ReviewInfo review) {

    }

    /**
     * Respond to a user's chat message with a response from an AI assistant.
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