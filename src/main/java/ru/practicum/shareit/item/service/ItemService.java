package ru.practicum.shareit.item.service;

import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto) throws NotFoundException, ValidationException;

    ItemDto update(Long userId, Long itemId, ItemDto itemDto) throws NotFoundException, ForbiddenException;

    ItemDto getById(Long itemId) throws NotFoundException;

    List<ItemDto> getAllByOwner(Long userId) throws NotFoundException;

    List<ItemDto> search(String text);
}