package com.svjmart.service;

import com.svjmart.dto.RegisterRequestDto;
import com.svjmart.entity.Role;
import com.svjmart.entity.SellerProfile;
import com.svjmart.entity.User;
import com.svjmart.repository.SellerProfileRepository;
import com.svjmart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       SellerProfileRepository sellerProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User registerUser(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with email " + dto.getEmail() + " already exists.");
        }

        Role assignedRole = "SELLER".equalsIgnoreCase(dto.getRole()) ? Role.ROLE_SELLER : Role.ROLE_BUYER;
        User user = new User(
                dto.getName(),
                dto.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getPhone(),
                assignedRole
        );

        User savedUser = userRepository.save(user);

        if (assignedRole == Role.ROLE_SELLER) {
            String store = dto.getStoreName() != null && !dto.getStoreName().isBlank()
                    ? dto.getStoreName()
                    : dto.getName() + " Store";
            SellerProfile profile = new SellerProfile(
                    savedUser,
                    store,
                    dto.getBusinessDescription(),
                    dto.getGstin(),
                    false // Requires Admin approval
            );
            sellerProfileRepository.save(profile);
            savedUser.setSellerProfile(profile);
        }

        return savedUser;
    }

    @Transactional
    public void updateProfile(Long userId, String name, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setName(name);
        user.setPhone(phone);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
}
