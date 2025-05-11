package org.zerox80.coingeckowebapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerox80.coingeckowebapp.model.User;
import org.zerox80.coingeckowebapp.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortfolioService portfolioService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PortfolioService portfolioService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.portfolioService = portfolioService;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User registerNewUser(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username " + username + " already exists.");
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        User savedUser = userRepository.save(newUser);
        portfolioService.createInitialPortfolio(savedUser);
        return savedUser;
    }
}