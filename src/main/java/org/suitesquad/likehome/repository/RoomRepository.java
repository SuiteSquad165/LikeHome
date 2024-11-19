package org.suitesquad.likehome.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.suitesquad.likehome.model.Room;

import java.util.List;

public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByHotelId(String hotelId);
}