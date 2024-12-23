package org.suitesquad.likehome.repository;

import org.suitesquad.likehome.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByEmail(String email);
}

