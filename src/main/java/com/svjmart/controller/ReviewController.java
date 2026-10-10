package com.svjmart.controller;

import com.svjmart.dto.ReviewDto;
import com.svjmart.entity.User;
import com.svjmart.service.ReviewService;
import com.svjmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @PostMapping("/submit")
    public String submitReview(@Valid @ModelAttribute("reviewDto") ReviewDto dto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please ensure your rating and review feedback are filled out.");
            return "redirect:/product/" + dto.getProductId();
        }

        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            reviewService.addReview(user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your product review has been submitted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/product/" + dto.getProductId();
    }
}
