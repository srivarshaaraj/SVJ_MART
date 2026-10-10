package com.svjmart.service;

import com.svjmart.dto.AddressDto;
import com.svjmart.entity.Address;
import com.svjmart.entity.User;
import com.svjmart.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getAddressesForUser(User user) {
        return addressRepository.findByUser(user);
    }

    public Optional<Address> getAddressById(Long id, User user) {
        return addressRepository.findByIdAndUser(id, user);
    }

    public Optional<Address> getDefaultAddress(User user) {
        return addressRepository.findByUserAndIsDefaultTrue(user);
    }

    @Transactional
    public Address saveOrUpdateAddress(User user, AddressDto dto) {
        Address address;
        if (dto.getId() != null) {
            address = addressRepository.findByIdAndUser(dto.getId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("Address not found or unauthorized"));
        } else {
            address = new Address();
            address.setUser(user);
        }

        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPincode(dto.getPincode());
        address.setLandmark(dto.getLandmark());

        List<Address> existing = addressRepository.findByUser(user);
        if (existing.isEmpty() || dto.isDefault()) {
            for (Address a : existing) {
                a.setDefault(false);
            }
            address.setDefault(true);
        } else {
            address.setDefault(dto.isDefault());
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void setDefaultAddress(Long addressId, User user) {
        List<Address> addresses = addressRepository.findByUser(user);
        for (Address a : addresses) {
            a.setDefault(a.getId().equals(addressId));
        }
        addressRepository.saveAll(addresses);
    }

    @Transactional
    public void deleteAddress(Long addressId, User user) {
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found or unauthorized"));
        addressRepository.delete(address);
    }
}
