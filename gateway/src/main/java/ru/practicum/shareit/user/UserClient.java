package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post(API_PREFIX, userDto);
    }

    public ResponseEntity<Object> updateUser(long userId, UserDto userDto) {
        return patch(API_PREFIX + "/{userId}", userId, Map.of("userId", userId), userDto);
    }

    public ResponseEntity<Object> getUser(long userId) {
        return get(API_PREFIX + "/{userId}", userId, Map.of("userId", userId));
    }

    public ResponseEntity<Object> getAllUsers() {
        return get(API_PREFIX);
    }

    public ResponseEntity<Object> deleteUser(long userId) {
        return delete(API_PREFIX + "/{userId}", userId, Map.of("userId", userId));
    }
}