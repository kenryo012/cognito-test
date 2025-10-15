package com.example.cognitotest.controller;

import com.example.cognitotest.service.CognitoUserService;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private final CognitoUserService cognitoUserService;

    public UserController(CognitoUserService cognitoUserService) {
        this.cognitoUserService = cognitoUserService;
    }

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        model.addAttribute("oidcUser", oidcUser);
        return "index";
    }

    @GetMapping("/me")
    public String userProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        model.addAttribute("authenticated", authenticated);

        if (!authenticated) {
            return "user";
        }

        Optional<Map<String, String>> attributes = cognitoUserService.getUserAttributes(authentication);
        attributes.ifPresent(map -> model.addAttribute("attributes", map));

        return "user";
    }
}
