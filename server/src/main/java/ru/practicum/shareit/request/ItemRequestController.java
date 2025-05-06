package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import java.util.List;


/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.create(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequester(
            @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getAllByRequester(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllOtherUsersRequests(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.getAllOtherUsersRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long requestId) {
        return itemRequestService.getById(requestId, userId);
    }
}
