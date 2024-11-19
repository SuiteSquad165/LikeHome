package org.suitesquad.likehome.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.suitesquad.likehome.model.Room;
import org.suitesquad.likehome.repository.RoomRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepo;

    public List<Room> fetchAllRooms() {
        return roomRepo.findAll();
    }

    public List<Room> findByHotelId(String hotelId) {
        return roomRepo.findByHotelId(hotelId);
    }

    public Optional<Room> findById(String id) {
        return roomRepo.findById(id);
    }
}