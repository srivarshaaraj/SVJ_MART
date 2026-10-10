package com.svjmart.service;

import com.svjmart.entity.SellerProfile;
import com.svjmart.entity.User;
import com.svjmart.repository.SellerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    private final SellerProfileRepository sellerProfileRepository;

    public SellerService(SellerProfileRepository sellerProfileRepository) {
        this.sellerProfileRepository = sellerProfileRepository;
    }

    public Optional<SellerProfile> getProfileByUser(User user) {
        return sellerProfileRepository.findByUser(user);
    }

    public List<SellerProfile> getAllSellers() {
        return sellerProfileRepository.findAll();
    }

    public List<SellerProfile> getPendingSellers() {
        return sellerProfileRepository.findByApproved(false);
    }

    public long countPendingSellers() {
        return sellerProfileRepository.countByApproved(false);
    }

    public long countApprovedSellers() {
        return sellerProfileRepository.countByApproved(true);
    }

    @Transactional
    public void approveSeller(Long profileId) {
        SellerProfile profile = sellerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Seller profile not found"));
        profile.setApproved(true);
        profile.setApprovedAt(LocalDateTime.now());
        sellerProfileRepository.save(profile);
    }

    @Transactional
    public void rejectOrSuspendSeller(Long profileId) {
        SellerProfile profile = sellerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Seller profile not found"));
        profile.setApproved(false);
        sellerProfileRepository.save(profile);
    }
}
