package org.suitesquad.likehome.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.suitesquad.likehome.model.Reservation;
import org.suitesquad.likehome.service.ReservationService;
import org.suitesquad.likehome.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles all administrator requests.
 * This is accessible by any user with the ADMIN role.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ReservationService reservationService;
    @Autowired private UserService userService;

    /**
     * Get a specific reservation for this user by ID.
     */
    @GetMapping(path = "/reservations/{reservationId}")
    public RestTypes.ReservationInfo getReservationById(@PathVariable String reservationId) {
        Reservation reservation = reservationService.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation '" + reservationId + "' does not exist!"));

        return new RestTypes.ReservationInfo(
                reservation.getId(),
                reservation.getHotelId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getCheckIn(),
                reservation.getCheckOut());
    }

    @GetMapping("/reservations/user/{userId}")
    public List<RestTypes.ReservationInfo> getReservationsByUserId(@PathVariable String userId) {
        if (userService.findById(userId).isEmpty()) {
            throw new RuntimeException("User '" + userId + "' does not exist!");
        }

        List<Reservation> reservations = reservationService.findByUserId(userId);

        var reservationInfos = new ArrayList<RestTypes.ReservationInfo>();
        for (Reservation reservation : reservations) {
            reservationInfos.add(new RestTypes.ReservationInfo(
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
}
