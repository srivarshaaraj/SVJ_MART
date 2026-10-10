package com.svjmart.service;

import com.svjmart.entity.NewsletterSubscription;
import com.svjmart.repository.NewsletterSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewsletterService {

    private final NewsletterSubscriptionRepository subscriptionRepository;

    public NewsletterService(NewsletterSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public boolean subscribe(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        String cleanEmail = email.toLowerCase().trim();
        if (subscriptionRepository.existsByEmail(cleanEmail)) {
            return false; // already subscribed
        }
        subscriptionRepository.save(new NewsletterSubscription(cleanEmail));
        return true;
    }

    public List<NewsletterSubscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }
}
