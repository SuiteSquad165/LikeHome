package org.suitesquad.likehome.rest;

import org.springframework.web.bind.annotation.*;
import org.suitesquad.likehome.model.Hotel;
import org.suitesquad.likehome.model.Reservation;
import org.suitesquad.likehome.model.User;
import org.suitesquad.likehome.rest.ResponseTypes.Room;
import org.suitesquad.likehome.service.HotelService;
import org.suitesquad.likehome.service.ReservationService;
import org.suitesquad.likehome.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    // Constructor injector
    public PublicController(UserService userService, HotelService hotelService, ReservationService reservationService) {
        this.userService = userService;
        this.hotelService = hotelService;
        this.reservationService = reservationService;
    }

    @GetMapping("/userdb")
    public String userDb() {
        List<User> userList = userService.fetchAllUserData();
        if (userList != null && !userList.isEmpty()) {
            return userList.getFirst().getEmail();  // returns the email of the first user in our database
        }
        return "Failure, no users found";
    }

    @GetMapping("/hoteldb")
    public String hotelDb() {
        List<Hotel> hotelList = hotelService.fetchAllHotelData();
        if (hotelList != null && !hotelList.isEmpty()) {
            return hotelList.getFirst().getName();  // returns the name of the first hotel in our database
        }
        return "Failure, no hotels found";
    }

    @GetMapping("/reservedb")
    public String reservationDb() {
        List<Reservation> reservationList = reservationService.fetchAllReservationData();
        if (reservationList != null && !reservationList.isEmpty()) {
            //System.out.println("Found reservations: " + reservationList.size());
            //System.out.println(reservationList);
            return reservationList.getFirst().getUserId(); // returns the userId of the first reservation in our database
        }
        return "Failure, no reservations found";
    }


    /**
     * Retrieve a list of all rooms with optional filters.
     * @param filters a set of filters to apply to the room search (for example, type=apartment)
     */
    @GetMapping("/rooms")
    public List<Room> getAllRooms(@RequestParam(required = false) Map<String, String> filters) {
        return List.of(Room.sample);
    }

    @GetMapping("/rooms/{roomId}")
    public Room getRoomById(@PathVariable(required = false) String roomId) {
        return Stream.of(Room.sample).filter(r -> r.id().equals(roomId)).findAny().orElseThrow();
    }

}
