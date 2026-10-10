package com.svjmart.controller;

import com.svjmart.dto.RegisterRequestDto;
import com.svjmart.entity.User;
import com.svjmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please verify your credentials.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out securely. See you again soon!");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please sign in with your credentials.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(value = "role", defaultValue = "BUYER") String role, Model model) {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setRole(role);
        model.addAttribute("registerDto", dto);
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerDto") RegisterRequestDto dto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (dto.getPassword() != null && dto.getConfirmPassword() != null &&
                !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerDto", "Passwords do not match.");
        }

        if (userService.emailExists(dto.getEmail())) {
            bindingResult.rejectValue("email", "error.registerDto", "An account with this email already exists.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            User saved = userService.registerUser(dto);
            if ("SELLER".equalsIgnoreCase(dto.getRole())) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Seller account submitted! Your store is pending administrator approval. You can log in once reviewed.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Registration successful! You can now log in to start smart shopping.");
                return "redirect:/login?registered=true";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage",
                "If an account with email " + email + " exists, a password reset link has been dispatched.");
        return "redirect:/login";
    }
}
