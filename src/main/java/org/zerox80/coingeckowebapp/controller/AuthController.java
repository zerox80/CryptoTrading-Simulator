package org.zerox80.coingeckowebapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.zerox80.coingeckowebapp.service.UserService;
import org.springframework.http.MediaType;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(ServerWebExchange exchange, Model model) {
        logger.info("Accessing /login page. Current URL in exchange: {}", exchange.getRequest().getURI());
        logger.info("Query Params from ServerWebExchange:");
        exchange.getRequest().getQueryParams().forEach((key, values) -> {
            logger.info("  Param: {} = {}", key, String.join(",", values));
        });

        boolean showError = exchange.getRequest().getQueryParams().containsKey("error");
        boolean showLogout = exchange.getRequest().getQueryParams().containsKey("logout");

        model.addAttribute("displayLoginError", showError);
        model.addAttribute("displayLogoutMessage", showLogout);
        
        logger.info("Model Attributes being set: displayLoginError={}, displayLogoutMessage={}", showError, showLogout);
        model.asMap().forEach((key, value) -> {
            logger.info("  Final Model Attr: {} = {}", key, value);
        });
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> registerUser(ServerWebExchange exchange, Model model) {
        return exchange.getFormData().flatMap(formData -> {
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");

            logger.info("Benutzerregistrierung versucht für Benutzer: {}", username);
            
            if (username == null || username.trim().isEmpty()) {
                logger.error("Leerer Benutzername bei der Registrierung");
                model.addAttribute("error", "Benutzername darf nicht leer sein.");
                return Mono.just("register");
            }
            
            if (password == null || password.trim().isEmpty()) {
                logger.error("Leeres Passwort bei der Registrierung");
                model.addAttribute("error", "Passwort darf nicht leer sein.");
                return Mono.just("register");
            }
            
            try {
                userService.registerNewUser(username, password);
                logger.info("Benutzer erfolgreich registriert: {}", username);
                return Mono.just("redirect:/login?registrationSuccess");
            } catch (RuntimeException e) {
                logger.error("Fehler bei der Benutzerregistrierung: {}", e.getMessage());
                model.addAttribute("error", e.getMessage());
                return Mono.just("register");
            }
        }).switchIfEmpty(Mono.defer(() -> {
            logger.error("Formulardaten nicht gefunden im Request.");
            model.addAttribute("error", "Fehler beim Verarbeiten der Formulardaten.");
            return Mono.just("register");
        }));
    }
    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        logger.error("Unerwarteter Fehler: ", e);
        model.addAttribute("error", "Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
        return "register";
    }
}