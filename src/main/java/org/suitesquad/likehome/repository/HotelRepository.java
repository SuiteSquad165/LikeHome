package org.suitesquad.likehome.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.suitesquad.likehome.model.Hotel;

import java.util.List;

@Repository
public interface HotelRepository extends MongoRepository<Hotel, String> {
    List<Hotel> findByName(String name);
    //List<Hotel> findAboveRating(double rating);
    //List<Hotel> findByLocation();
}
