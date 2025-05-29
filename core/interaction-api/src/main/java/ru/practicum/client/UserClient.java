package ru.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.user.model.User;

import java.util.Optional;

public interface UserClient {
    @GetMapping("/{id}")
    Optional<User> getUserById(@PathVariable Long id) throws FeignException;
}
