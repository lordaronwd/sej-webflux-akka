package com.zooplus.sej.msone;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author lazar.agatonovic
 */
public interface UserRepository extends CrudRepository<User, Long> {

    default CompletableFuture<Optional<User>> getUser(Long id) {
        return CompletableFuture.supplyAsync(() -> findById(id));
    }

    default CompletableFuture<User> createUser(User user) {
        return CompletableFuture.supplyAsync(() -> save(user));
    }
}
