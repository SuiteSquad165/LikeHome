package org.suitesquad.likehome.rest;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.suitesquad.likehome.model.Hotel;
import org.suitesquad.likehome.model.Reservation;
import org.suitesquad.likehome.model.Room;
import org.suitesquad.likehome.model.User;
import org.suitesquad.likehome.rest.RestTypes.HotelInfo;
import org.suitesquad.likehome.rest.RestTypes.RoomInfo;
import org.suitesquad.likehome.service.HotelService;
import org.suitesquad.likehome.service.ReservationService;
import org.suitesquad.likehome.service.RoomService;
import org.suitesquad.likehome.service.UserService;

import java.util.*;

/**
 * This class handles all requests not requiring authentication.
 */
@RestController
@RequestMapping
public class PublicController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    private final UserService userService;
    private final HotelService hotelService;
    private final ReservationService reservationService;
    private final RoomService roomService;

    // Constructor injector
    public PublicController(UserService userService, HotelService hotelService, ReservationService reservationService, RoomService roomService) {
        this.userService = userService;
        this.hotelService = hotelService;
        this.reservationService = reservationService;
        this.roomService = roomService;
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
     * Retrieve a list of all rooms with optional filters.
     * @param filters a set of filters to apply to the room search (for example, type=apartment)
     */
    @GetMapping("/hotels")
    public List<HotelInfo> getAllHotels(@RequestBody(required = false) Map<String, Object> filters) {
        List<HotelInfo> hotels = new ArrayList<>();

        // 0 = default, 1 = rating
        int sort = 0;

        Query query = new Query();
        for (String key : filters.keySet()) {
            if(key.equalsIgnoreCase("sort"))
            {
                sort = switch ((String) filters.get(key)) {
                    case "rating" -> 1;
                    default -> 0;
                };
            }
            query.addCriteria(Criteria.where(key).is(filters.get(key)));
        }

        List<Hotel> fetchedHotelsQuery = hotelService.findAllByQuery(query);

        if(sort == 1) {
            fetchedHotelsQuery.sort(Comparator.comparingDouble(Hotel::getRating).reversed());
        }

        for (Hotel hotel : fetchedHotelsQuery) {
            hotels.add(new HotelInfo(
                    hotel.getId(),
                    hotel.getName(),
                    hotel.getDescription(),
                    hotel.getRating(),
                    hotel.getReviews().size(),
                    hotel.getLocation().getCity(),
                    hotel.getImageUrls(),
                    hotel.getRoomIds()
            ));
        }

        return hotels;
    }

    @GetMapping("/hotels/{hotelId}")
    public HotelInfo getHotelById(@PathVariable String hotelId,
                                  @RequestParam(required = false) Map<String, String> filters) {
        HotelInfo hotelInfo;

        Hotel hotel = hotelService.findById(hotelId);
        if(hotel == null) {
            throw new RuntimeException("Hotel '" + hotelId + "' not found");
        }

        hotelInfo = new HotelInfo(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getRating(),
                hotel.getReviews().size(),
                hotel.getLocation().getCity(),
                hotel.getImageUrls(),
                hotel.getRoomIds());

        return hotelInfo;
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public List<RoomInfo> getHotelRooms(@PathVariable String hotelId,
                                        @RequestParam(required = false) Map<String, String> filters) {
        List<RoomInfo> rooms = new ArrayList<>();

        Hotel hotel = hotelService.findById(hotelId);
        if(hotel == null) {
            throw new RuntimeException("Hotel '" + hotelId + "' not found");
        }

        // 0 = default, 1 = rating, 2 = price
        int sort = 0;

        for (String key : filters.keySet()) {
            if(key.equalsIgnoreCase("sort"))
            {
                sort = switch ((String) filters.get(key)) {
                    case "rating" -> 1;
                    case "price" -> 2;
                    default -> 0;
                };
            }
        }

        List<Room> fetchedRooms = roomService.findByHotelId(hotelId);

        if(sort == 1) {
            fetchedRooms.sort(Comparator.comparingDouble(Room::getRating).reversed());
        } else if(sort == 2)
        {
            fetchedRooms.sort(Comparator.comparingDouble(Room::getPrice));
        }

        for (Room room : fetchedRooms) {
            rooms.add(new RoomInfo(
                    room.getId(),
                    room.getRoomType(),
                    room.getPrice(),
                    room.getRating(),
                    room.getFeatures(),
                    room.getImageUrls()
            ));
        }

        return rooms;
    }

    @GetMapping("/hotels/{hotelId}/rooms/{roomId}")
    public RoomInfo getHotelRoomById(@PathVariable String hotelId, @PathVariable String roomId,
                                     @RequestParam(required = false) Map<String, String> filters) {
        RoomInfo room;

        Hotel hotel = hotelService.findById(hotelId);
        if(hotel == null) {
            throw new RuntimeException("Hotel '" + hotelId + "' not found");
        }

        Room fetchedRoom = roomService.findById(roomId).isPresent() ? roomService.findById(roomId).get() : null;
        if(fetchedRoom == null) {
            throw new RuntimeException("Room '" + roomId + "' not found");
        }

        room = new RoomInfo(
                fetchedRoom.getId(),
                fetchedRoom.getRoomType(),
                fetchedRoom.getPrice(),
                fetchedRoom.getRating(),
                fetchedRoom.getFeatures(),
                fetchedRoom.getImageUrls()
        );

        return room;
    }
}