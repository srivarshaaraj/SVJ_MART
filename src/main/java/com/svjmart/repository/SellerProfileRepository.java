package com.svjmart.repository;

import com.svjmart.entity.SellerProfile;
import com.svjmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUser(User user);
    List<SellerProfile> findByApproved(boolean approved);
    long countByApproved(boolean approved);
}
