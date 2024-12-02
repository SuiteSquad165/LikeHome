package org.suitesquad.likehome.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.suitesquad.likehome.model.Reservation;
import org.suitesquad.likehome.repository.ReservationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepo;

    public Reservation addReservationData(Reservation reservationDetails) {
        return reservationRepo.insert(reservationDetails);
    }

    public void addMultipleReservationData(List<Reservation> reservationDetails) {
        reservationRepo.insert(reservationDetails);
    }

    public List<Reservation> fetchAllReservationData() {
        return reservationRepo.findAll();
    }

    public void deleteReservationData(Reservation reservationDetails) {
        reservationRepo.delete(reservationDetails);
    }

    public void deleteAllReservationData() {
        reservationRepo.deleteAll();
    }

    public List<Reservation> findByUserId(String userId) {
        return reservationRepo.findByUserId(userId);
    }

    public List<Reservation> findByHotelId(String hotelId) {
        return reservationRepo.findByHotelId(hotelId);
    }

    public Optional<Reservation> findById(String id) {
        return reservationRepo.findById(id);
    }

    public List<Reservation> findByUserIdAndHotelId(String userId, String hotelId) {
        return reservationRepo.findByUserIdAndHotelId(userId, hotelId);
    }

    public void deleteById(String id) {
        reservationRepo.deleteById(id);
    }

    public void save(Reservation reservation) {
        reservationRepo.save(reservation);
    }
}
