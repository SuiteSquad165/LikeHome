package org.suitesquad.likehome.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.suitesquad.likehome.model.Hotel;
import org.suitesquad.likehome.repository.HotelRepository;

import java.util.List;

@Service
public class HotelService {
    @Autowired
    private HotelRepository hotelRepo;

    public void addHotelData(Hotel userDetails) {
        hotelRepo.insert(userDetails);
    }

    public void addMultipleHotelData(List<Hotel> userDetail) {
        hotelRepo.insert(userDetail);
    }

    public List<Hotel> fetchAllHotelData() {
        return hotelRepo.findAll();
    }

    public void deleteHotelData(Hotel userDetails) {
        hotelRepo.delete(userDetails);
    }

    public void deleteAllHotelData() {
        hotelRepo.deleteAll();
    }

    public List<Hotel> findByName(String name){
        return hotelRepo.findByName(name);
    }

    /*
    public List<Hotel> findAboveRating(double rating){
        return hotelRepo.findAboveRating(rating);
    }
     */
}
