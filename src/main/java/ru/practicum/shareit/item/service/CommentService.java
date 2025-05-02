package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto create(Long userId, Long itemId, CommentDto commentDto) throws NotFoundException, ValidationException;

    List<CommentDto> getCommentsByItemId(Long itemId);
}