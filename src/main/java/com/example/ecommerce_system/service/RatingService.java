package com.example.ecommerce_system.service;

import com.example.ecommerce_system.model.Rating;
import com.example.ecommerce_system.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RatingService {
    @Autowired
    private RatingRepository ratingRepository;

    public Rating createRating(Rating rating) {
        return ratingRepository.save(rating);
    }

    public Optional<Rating> getRatingById(Long id) {
        return ratingRepository.findById(id);
    }

    public List<Rating> getRatingsByProductId(Long productId) {
        return ratingRepository.findByProductId(productId);
    }

    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }

    public void deleteRating(Long id) {
        ratingRepository.deleteById(id);
    }
}
