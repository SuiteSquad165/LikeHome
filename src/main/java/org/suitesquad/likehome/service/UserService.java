package org.suitesquad.likehome.service;

import org.suitesquad.likehome.model.User;
import org.suitesquad.likehome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    public void addUserData(User userDetails) {
        userRepo.insert(userDetails);
    }

    public void addMultipleUserData(List<User> userDetail) {
        userRepo.insert(userDetail);
    }

    public List<User> fetchAllUserData() {
        return userRepo.findAll();
    }

    public void deleteUserData(User userDetails) {
        userRepo.delete(userDetails);
    }

    public void deleteAllUserData() {
        userRepo.deleteAll();
    }

    public List<User> findByEmail(String email){
        return userRepo.findByEmail(email);
    }
}