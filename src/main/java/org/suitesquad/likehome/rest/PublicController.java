package org.suitesquad.likehome.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.suitesquad.likehome.model.*;
import org.suitesquad.likehome.rest.RestTypes.HotelInfo;
import org.suitesquad.likehome.rest.RestTypes.ReviewInfo;
import org.suitesquad.likehome.rest.RestTypes.RoomInfo;
import org.suitesquad.likehome.service.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class handles all requests not requiring authentication.
 */
@RestController
@RequestMapping
@Tag(name = "Public", description = "No Authentication Required.")
public class PublicController {

    @Autowired private UserService userService;
    @Autowired private HotelService hotelService;
    @Autowired private ReservationService reservationService;
    @Autowired private RoomService roomService;
    @Autowired private ReviewService reviewService;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/userdb")
    public List<User> userDb() {
        return userService.fetchAllUserData();
    }

    @GetMapping("/hoteldb")
    public List<Hotel> hotelDb() {
        return hotelService.fetchAllHotelData();
    }

    @GetMapping("/reservedb")
    public List<Reservation> reservationDb() {
        return reservationService.fetchAllReservationData();
    }

    /**
     * Get a JWT token for the user. Convenience method for testing.
     */
    @PostMapping("/token")
    public String getToken(@RequestBody TokenRequest request) throws IOException, InterruptedException {
        var httpRequest = HttpRequest.newBuilder(URI.create(
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + request.firebaseApiKey()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        { "email": "%s", "password": "%s", "returnSecureToken": true }
                        """.formatted(request.email(), request.password())))
                .build();
        try (var client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return new ObjectMapper().readTree(response.body())
                    .get("idToken").asText();
        }
    }

    public record TokenRequest(String firebaseApiKey, String email, String password) {}

    /**
     * Retrieve a list of all rooms with optional filters.
     *
     * @param filters a set of filters to apply to the room search (for example, type=apartment)
     */
    @GetMapping("/hotels")
    public List<HotelInfo> getAllHotels(@RequestBody(required = false) Map<String, Object> filters) {
        var query = new Query();
        for (String key : filters.keySet()) {
            query.addCriteria(Criteria.where(key).is(filters.get(key)));
        }

        List<Hotel> fetchedHotelsQuery = hotelService.findAllByQuery(query);

        if ("rating".equals(filters.get("sort"))) {
            fetchedHotelsQuery.sort(Comparator.comparingDouble(Hotel::getRating).reversed());
        }

        var hotels = new ArrayList<HotelInfo>();
        for (Hotel hotel : fetchedHotelsQuery) {
            var roomIds = roomService.findByHotelId(hotel.getId()).stream().map(Room::getId).toList();
            hotels.add(new HotelInfo(
                    hotel.getId(),
                    hotel.getName(),
                    hotel.getDescription(),
                    hotel.getRating(),
                    reviewService.findByHotelId(hotel.getId()).size(),
                    hotel.getLocation().getCity(),
                    hotel.getImageUrls(),
                    roomIds
            ));
        }

        return hotels;
    }

    @GetMapping("/hotels/{hotelId}")
    public HotelInfo getHotelById(@PathVariable String hotelId) {
        Hotel hotel = hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        var roomIds = roomService.findByHotelId(hotel.getId()).stream().map(Room::getId).toList();

        return new HotelInfo(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getRating(),
                reviewService.findByHotelId(hotel.getId()).size(),
                hotel.getLocation().getCity(),
                hotel.getImageUrls(),
                roomIds);
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public List<RoomInfo> getHotelRooms(@PathVariable String hotelId,
                                        @RequestParam(required = false) Map<String, String> filters) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        List<Room> fetchedRooms = roomService.findByHotelId(hotelId);

        if ("price".equals(filters.get("sort"))) {
            fetchedRooms.sort(Comparator.comparingDouble(Room::getPricePerNight));
        }

        var rooms = new ArrayList<RoomInfo>();
        for (Room room : fetchedRooms) {
            rooms.add(new RoomInfo(
                    room.getId(),
                    room.getName(),
                    room.getBaths(),
                    room.getBeds(),
                    room.getGuests(),
                    room.getPricePerNight(),
                    room.getAmenities(),
                    room.getImageUrls()
            ));
        }

        return rooms;
    }

    @GetMapping("/hotels/{hotelId}/rooms/{roomId}")
    public RoomInfo getHotelRoomById(@PathVariable String hotelId, @PathVariable String roomId) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));


        Room fetchedRoom = roomService.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room '" + roomId + "' not found"));

        return new RoomInfo(
                fetchedRoom.getId(),
                fetchedRoom.getName(),
                fetchedRoom.getBaths(),
                fetchedRoom.getBeds(),
                fetchedRoom.getGuests(),
                fetchedRoom.getPricePerNight(),
                fetchedRoom.getAmenities(),
                fetchedRoom.getImageUrls()
        );
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    public List<ReviewInfo> getReviewByHotelId(@PathVariable String hotelId) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));


        List<Review> reviews = reviewService.findByHotelId(hotelId);

        var reviewInfos = new ArrayList<ReviewInfo>();
        for (Review review : reviews) {
            reviewInfos.add(new ReviewInfo(
                    review.getId(),
                    review.getUserId(),
                    review.getContents(),
                    review.getRating(),
                    review.getReviewDate()
            ));
        }

        return reviewInfos;
    }
}