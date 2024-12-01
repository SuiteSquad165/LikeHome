package org.suitesquad.likehome.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.suitesquad.likehome.model.Room;

import java.util.List;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByHotelId(String hotelId);
}