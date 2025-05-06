package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(itemRequest.getItems().stream()
                        .map(item -> ItemResponseDto.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .ownerId(item.getOwner().getId())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requester) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }
}