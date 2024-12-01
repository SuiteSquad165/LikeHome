package org.suitesquad.likehome.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.suitesquad.likehome.model.Hotel;
import org.suitesquad.likehome.model.Review;
import org.suitesquad.likehome.model.Room;
import org.suitesquad.likehome.rest.RestTypes.HotelInfo;
import org.suitesquad.likehome.rest.RestTypes.ReviewInfo;
import org.suitesquad.likehome.rest.RestTypes.RoomInfo;
import org.suitesquad.likehome.service.HotelService;
import org.suitesquad.likehome.service.ReviewService;
import org.suitesquad.likehome.service.RoomService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * This class handles all requests not requiring authentication.
 */
@RestController
@RequestMapping
@Tag(name = "Public", description = "No Authentication Required.")
public class PublicController {

    @Autowired private HotelService hotelService;
    @Autowired private RoomService roomService;
    @Autowired private ReviewService reviewService;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
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
     * Retrieve a list of all hotels with optional sort by rating. All filters are case-insensitive.
     *
     * @param sort     Sort by rating if "rating" is passed
     * @param location city, state, or country contains
     * @param name     hotel name contains
     * @param rating   minimum rating
     */
    @GetMapping("/hotels")
    public List<HotelInfo> getAllHotels(@RequestParam(required = false) String sort,
                                        @RequestParam(required = false) String location,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) Double rating) {
        var hotels = hotelService.findAll().stream();

        if ("rating".equalsIgnoreCase(sort)) {
            hotels = hotels.sorted(Comparator.comparingDouble(Hotel::getRating).reversed());
        }
        if (isNotBlank(location)) {
            hotels = hotels.filter(hotel ->
                    containsIgnoreCase(hotel.getLocation().getCity(), location)
                    || containsIgnoreCase(hotel.getLocation().getState(), location)
                    || containsIgnoreCase(hotel.getLocation().getCountry(), location)
            );
        }
        if (isNotBlank(name)) {
            hotels = hotels.filter(hotel -> containsIgnoreCase(hotel.getName(), name));
        }
        if (rating != null && rating > 0) {
            hotels = hotels.filter(hotel -> hotel.getRating() >= rating);
        }

        return hotels.map(hotel -> new HotelInfo(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getRating(),
                reviewService.findByHotelId(hotel.getId()).size(),
                hotel.getLocation().getCity(),
                hotel.getImageUrls(),
                roomService.findByHotelId(hotel.getId()).stream().map(Room::getId).toList()
        )).toList();
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

        return fetchedRooms.stream().map(room -> new RoomInfo(
                room.getId(),
                room.getName(),
                room.getBaths(),
                room.getBeds(),
                room.getGuests(),
                room.getBedrooms(),
                room.getDescription(),
                room.getPricePerNight(),
                room.getAmenities(),
                room.getImageUrls()
        )).toList();
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
                fetchedRoom.getBedrooms(),
                fetchedRoom.getDescription(),
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