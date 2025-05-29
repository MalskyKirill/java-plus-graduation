package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/internal/api/users", contextId = "userServiceClient")
public interface UserServiceClient extends UserClient {
}
