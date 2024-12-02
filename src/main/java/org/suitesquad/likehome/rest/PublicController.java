package org.suitesquad.likehome.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.suitesquad.likehome.model.Hotel;
import org.suitesquad.likehome.model.Room;
import org.suitesquad.likehome.rest.RestTypes.HotelInfo;
import org.suitesquad.likehome.rest.RestTypes.ReviewInfo;
import org.suitesquad.likehome.service.HotelService;
import org.suitesquad.likehome.service.ReviewService;
import org.suitesquad.likehome.service.RoomService;
import org.suitesquad.likehome.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

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
    @Autowired private UserService userService;

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
     * Retrieve a list of all hotels with optional filters. All filters are case-insensitive.
     *
     * @param sort      Sort by rating if "rating" is passed
     * @param location  city, state, or country contains
     * @param name      hotel name contains
     * @param minRating minimum rating
     */
    @GetMapping("/hotels")
    public List<HotelInfo> getAllHotels(@RequestParam(required = false) String sort,
                                        @RequestParam(required = false) String location,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(defaultValue = "0") Double minRating) {
        var hotels = hotelService.findAll().stream();

        if ("rating".equalsIgnoreCase(sort)) {
            hotels = hotels.sorted(Comparator.comparingDouble(Hotel::getRating).reversed());
        }
        return hotels
                .filter(isBlank(location) ? noFilter()
                        : hotel -> containsIgnoreCase(hotel.getLocation().getCity(), location)
                                   || containsIgnoreCase(hotel.getLocation().getState(), location)
                                   || containsIgnoreCase(hotel.getLocation().getCountry(), location))
                .filter(isBlank(name) ? noFilter()
                        : hotel -> containsIgnoreCase(hotel.getName(), name))
                .filter(hotel -> hotel.getRating() >= minRating)
                .map(hotel -> new HotelInfo(
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

    /**
     * Retrieve a list of all rooms for a hotel with optional filters. All filters are case-insensitive.
     *
     * @param hotelId     hotel ID
     * @param sort        Sort by price if "price" is passed
     * @param name        room name contains
     * @param minBaths    minimum number of baths
     * @param minBeds     minimum number of beds
     * @param minGuests   minimum number of guests
     * @param minBedrooms minimum number of bedrooms
     * @param minPrice    minimum price per night
     * @param maxPrice    maximum price per night
     */
    @GetMapping("/hotels/{hotelId}/rooms")
    public List<Room> getHotelRooms(@PathVariable String hotelId,
                                        @RequestParam(required = false) String sort,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(defaultValue = "0") Integer minBaths,
                                        @RequestParam(defaultValue = "0") Integer minBeds,
                                        @RequestParam(defaultValue = "0") Integer minGuests,
                                        @RequestParam(defaultValue = "0") Integer minBedrooms,
                                        @RequestParam(defaultValue = "0") Double minPrice,
                                        @RequestParam(required = false) Double maxPrice) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        Stream<Room> rooms = roomService.findByHotelId(hotelId).stream();

        if ("price".equalsIgnoreCase(sort)) {
            rooms = rooms.sorted(Comparator.comparingDouble(Room::getPricePerNight));
        }
        return rooms
                .filter(isBlank(name) ? noFilter()
                        : room -> containsIgnoreCase(room.getName(), name))
                .filter(room -> room.getBaths() >= minBaths)
                .filter(room -> room.getBeds() >= minBeds)
                .filter(room -> room.getGuests() >= minGuests)
                .filter(room -> room.getBedrooms() >= minBedrooms)
                .filter(room -> room.getPricePerNight() >= minPrice)
                .filter(maxPrice == null ? noFilter()
                        : room -> room.getPricePerNight() <= maxPrice)
                .toList();
    }

    private static <T> Predicate<T> noFilter() { // for when no filter is applied
        return t -> true;
    }

    @GetMapping("/hotels/{hotelId}/rooms/{roomId}")
    public Room getHotelRoomById(@PathVariable String hotelId, @PathVariable String roomId) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        return roomService.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room '" + roomId + "' not found"));
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    public List<ReviewInfo> getReviewsByHotelId(@PathVariable String hotelId) {
        hotelService.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel '" + hotelId + "' not found"));

        return reviewService.findByHotelId(hotelId).stream()
                .map(review -> new ReviewInfo(
                        review.getId(),
                        userService.findById(review.getUserId()).get().getFirstName(),
                        review.getContents(),
                        review.getRating(),
                        review.getReviewDate()
                )).toList();
    }
}