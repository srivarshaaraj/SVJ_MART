package com.svjmart.controller;

import com.svjmart.dto.AddressDto;
import com.svjmart.entity.Address;
import com.svjmart.entity.User;
import com.svjmart.service.AddressService;
import com.svjmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final AddressService addressService;

    public ProfileController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        User user = getAuthenticatedUser(authentication);
        List<Address> addresses = addressService.getAddressesForUser(user);

        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("newAddress", new AddressDto());
        return "buyer/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam("phone") String phone,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        try {
            userService.updateProfile(user.getId(), name, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Profile details updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/address/add")
    public String addAddress(@Valid @ModelAttribute("newAddress") AddressDto addressDto,
                             BindingResult bindingResult,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide all required address details correctly.");
            return "redirect:/profile";
        }

        try {
            addressService.saveOrUpdateAddress(user, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Delivery address saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        try {
            addressService.deleteAddress(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Address removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/address/default/{id}")
    public String setDefaultAddress(@PathVariable("id") Long id,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        try {
            addressService.setDefaultAddress(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Default delivery address updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }
}
