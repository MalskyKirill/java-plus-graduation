package ru.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserClient {
    @GetMapping("/{id}")
    Optional<User> getUserById(@PathVariable Long id) throws FeignException;

    @GetMapping("/list")
    List<User> getUsersWithIds(@RequestParam List<Long> ids) throws FeignException;
}
