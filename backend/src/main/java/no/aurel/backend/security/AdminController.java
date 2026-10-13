package no.aurel.backend.security;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // Used by the login form: a 200 means the credentials were accepted (everything under /api/admin needs ADMIN)
    @GetMapping("/me")
    public AdminResponse me(Principal principal) {
        return new AdminResponse(principal.getName());
    }

    public record AdminResponse(String username) {
    }
}
