//package com.example.formicarium.controller;
//
//import com.example.formicarium.entity.Role;
//import com.example.formicarium.entity.User;
//import com.example.formicarium.repository.UserRepository;
//import com.example.formicarium.repository.RoleRepository;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
////@RestController
////@RequestMapping("/api")
////public class AuthController {
////
////    private final UserRepository userRepository;
////    private final RoleRepository roleRepository;
////    private final PasswordEncoder passwordEncoder;
////
////    public AuthController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
////        this.userRepository = userRepository;
////        this.roleRepository = roleRepository;
////        this.passwordEncoder = passwordEncoder;
////    }
////
////    @GetMapping("/protected")
////    public ResponseEntity<String> protectedEndpoint() {
////        return ResponseEntity.ok("Access granted to protected endpoint");
////    }
////
////    @PostMapping("/register")
////    public ResponseEntity<String> register(@RequestBody User user, @RequestParam(required = false) String role) {
////
////        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
////            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
////        }
////
////        // criptam parola
////        user.setPassword(passwordEncoder.encode(user.getPassword()));
////
////        // setarea rolului implicit sau rolul specificat
////        String roleName = (role != null) ? role.toUpperCase() : "USER";
////        Role userRole = roleRepository.findByName(roleName)
////                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
////
////        user.getRoles().add(userRole);
////        userRepository.save(user);
////        return ResponseEntity.ok("User registered with role: " + roleName);
////    }
////
////}
//
//@Controller
//public class AuthController {
//
//    @GetMapping("/login")
//    public String showLoginPage() {
//        return "login";
//    }
//
//    @GetMapping("/register")
//    public String showRegisterPage() {
//        return "register";
//    }
//}


//package com.example.formicarium.controller;
//
//import com.example.formicarium.entity.Role;
//import com.example.formicarium.entity.User;
//import com.example.formicarium.repository.UserRepository;
//import com.example.formicarium.repository.RoleRepository;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//public class AuthController {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public AuthController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.roleRepository = roleRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @GetMapping("/login")
//    public String showLoginPage() {
//        return "login";
//    }
//
//    @GetMapping("/register")
//    public String showRegisterPage() {
//        return "register";
//    }
//
//    @PostMapping("/api/register")
//    @ResponseBody
//    public ResponseEntity<String> register(@RequestBody User user, @RequestParam(required = false) String role) {
//        System.out.println("Register endpoint called"); // Log pentru debugging
//
//        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
//        }
//
//        // Criptarea parolei
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//
//        // Setarea rolului implicit sau specificat
//        String roleName = (role != null) ? role.toUpperCase() : "USER";
//        Role userRole = roleRepository.findByName(roleName)
//                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
//
//        user.getRoles().add(userRole);
//        userRepository.save(user);
//
//        return ResponseEntity.ok("User registered with role: " + roleName);
//    }
//}


package com.example.formicarium.controller;

import com.example.formicarium.entity.Role;
import com.example.formicarium.entity.User;
import com.example.formicarium.repository.UserRepository;
import com.example.formicarium.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
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

        // Setăm parola criptată
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Setăm rolul implicit "USER"
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "USER")));

        user.getRoles().add(userRole);
        userRepository.save(user);

        return "redirect:/login?registered";
    }
}

