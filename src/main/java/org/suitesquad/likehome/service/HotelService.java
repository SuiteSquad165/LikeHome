package org.suitesquad.likehome.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.suitesquad.likehome.model.Hotel;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
//    @Autowired
//    private HotelRepository hotelRepo;

    @Autowired
    private MongoTemplate hotelRepo;

    public void addHotelData(Hotel userDetails) {
        hotelRepo.insert(userDetails);
    }

    public void addMultipleHotelData(List<Hotel> userDetail) {
        hotelRepo.insert(userDetail);
    }

    public List<Hotel> fetchAllHotelData() {
        return hotelRepo.findAll(Hotel.class);
    }

    public void deleteHotelData(Hotel userDetails) {
        hotelRepo.remove(userDetails);
    }

    public void deleteAllHotelData() {
        hotelRepo.remove(Hotel.class);
    }

    public List<Hotel> findByName(String name) {
        var query = new Query();
        query.addCriteria(Criteria.where("name").is(name));

        return hotelRepo.find(query, Hotel.class);
    }

    public Optional<Hotel> findById(String id) {
        var query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));

        List<Hotel> hotels = hotelRepo.find(query, Hotel.class);
        return hotels.isEmpty() ? Optional.empty()
                : Optional.of(hotels.getFirst());
    }

    public List<Hotel> findAllByQuery(Query query) {
        return hotelRepo.find(query, Hotel.class);
    }

    public List<Hotel> findAll() {
        return hotelRepo.findAll(Hotel.class);
    }

    /*
    public List<Hotel> findAboveRating(double rating){
        return hotelRepo.findAboveRating(rating);
    }
     */
}