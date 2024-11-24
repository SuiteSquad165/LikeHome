package org.suitesquad.likehome.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.suitesquad.likehome.model.User;
import org.suitesquad.likehome.rest.RestTypes.ChatMessage;
import org.suitesquad.likehome.rest.RestTypes.Reservation;
import org.suitesquad.likehome.rest.RestTypes.ReviewInfo;
import org.suitesquad.likehome.rest.RestTypes.SignUpInfo;
import org.suitesquad.likehome.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * This class handles all authenticated requests.
 * Authenticated means the user has a valid JWT token.
 * Any user that is authenticated can access these endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthenticatedController {
    
    @Autowired private UserService userService;

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
    public List<Reservation> getReservations(JwtAuthenticationToken token) {
        return List.of(Reservation.sample);
    }

    /**
     * Get a specific reservation for this user by ID.
     */
    @GetMapping(path = "/reservations/{reservationId}")
    public Reservation getReservationById(JwtAuthenticationToken token, @PathVariable String reservationId) {
        return Stream.of(Reservation.sample).filter(r -> r.id().equals(reservationId)).findAny().orElseThrow();
    }

    /**
     * Create a reservation for this user.
     * The reservation.id field and reservation.userID fields are ignored.
     * Instead, the id is generated and assigned, and the user ID is retrieved from the token.
     */
    @PostMapping(path = "/reservations")
    public void createReservation(JwtAuthenticationToken token, @RequestBody Reservation reservation) {

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
