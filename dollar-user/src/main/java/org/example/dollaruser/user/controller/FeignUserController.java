package org.example.dollaruser.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.dollaruser.user.service.UserService;
import org.example.share.config.global.entity.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external")
public class FeignUserController {
    private final UserService userService;

    @GetMapping("/users/{userId}")
    public User getUser(@PathVariable("userId") Long userId) {
        return userService.findById(userId);
    }
}
