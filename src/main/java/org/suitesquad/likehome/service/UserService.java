package org.suitesquad.likehome.service;

import org.suitesquad.likehome.model.User;
import org.suitesquad.likehome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<User> findById(String id) {
        return userRepo.findById(id);
    }

    public void updateUserPoints(String id, int points)
    {
        Optional<User> user_optional = userRepo.findById(id);
        if(user_optional.isPresent()) {
            User user = user_optional.get();
            user.setPoints(points);
            userRepo.save(user);
        }
    }
}