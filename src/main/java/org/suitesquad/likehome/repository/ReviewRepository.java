package org.suitesquad.likehome.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.suitesquad.likehome.model.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByHotelId(String hotelId);

    void updateReviewById(String id, Review reviewDetails);
}
