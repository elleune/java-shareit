package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;


@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @RequestHeader(USER_ID_HEADER) long userId,
            @Valid @RequestBody ItemRequestCreateDto itemRequestDto) {
        return itemRequestClient.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByRequester(
            @RequestHeader(USER_ID_HEADER) long userId) {
        return itemRequestClient.getAllUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOtherUsersRequests(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return itemRequestClient.getAllOtherRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader(USER_ID_HEADER) long userId,
            @PathVariable long requestId) {
        return itemRequestClient.getRequest(userId, requestId);
    }
}