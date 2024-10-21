package org.suitesquad.likehome.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.suitesquad.likehome.model.Reservation;

import java.util.List;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String>{
    List<Reservation> findByUserId(String userId);
    List<Reservation> findByHotelId(String hotelId);
}