package org.suitesquad.likehome.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.suitesquad.likehome.service.UserService;
import org.suitesquad.likehome.model.User;

import javax.swing.*;
import java.util.List;

/**
 * This class handles all requests not requiring authentication.
 */
@RestController
@RequestMapping
public class PublicController {

    @Autowired
    private UserService userService;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    // Example of how to access database. Create a userService and perform the appropriate operation.
    @GetMapping("/db")
    public String db() {
        List<User> userList = userService.fetchAllUserData();
        if (userList != null && !userList.isEmpty()) {
            return userList.getFirst().getEmail();  // returns the email of the first user in our database
        }
        return "Failure, no users found";
    }
}
