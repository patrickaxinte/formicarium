package com.example.formicarium.controller;

import com.example.formicarium.entity.Role;
import com.example.formicarium.entity.User;
import com.example.formicarium.repository.RoleRepository;
import com.example.formicarium.repository.UserRepository;
import com.example.formicarium.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UserDetailsService userDetailsService, UserRepository userRepository,
                          RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpServletResponse response, Model model) {
        try {
            // autentificare utilizator
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // incarcare detalii utilizator
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // generare token JWT
            String jwtToken = jwtService.generateToken(userDetails);

            // setare token JWT in cookie
            int expirationInSeconds = jwtService.getExpirationInSeconds();

            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", jwtToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(expirationInSeconds)
                    .sameSite("Strict")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            // redirectionare catre dashboard
            return "redirect:/dashboard";
        } catch (BadCredentialsException e) {
            model.addAttribute("error", "Invalid username or password.");
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, Model model) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists.");
            return "register";
        }

        // setam parola criptata
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // setam rolul implicit "USER"
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "USER")));

        user.getRoles().add(userRole);
        userRepository.save(user);

        // redirectionam catre pagina de login cu un parametru de confirmare
        return "redirect:/login?registered";
    }

}
