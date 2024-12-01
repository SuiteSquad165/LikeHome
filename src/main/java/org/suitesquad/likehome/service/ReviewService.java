package org.suitesquad.likehome.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.suitesquad.likehome.model.Review;
import org.suitesquad.likehome.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> findByHotelId(String hotelId) {
        return reviewRepository.findByHotelId(hotelId);
    }

    public void addReviewData(Review reviewDetails) {
        reviewRepository.insert(reviewDetails);
    }

    public Optional<Review> findById(String id) {
        return reviewRepository.findById(id);
    }
}
