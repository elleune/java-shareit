package ru.practicum.shareit.user.service;

import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto) throws ValidationException, ConflictException;

    UserDto update(Long userId, UserDto userDto) throws NotFoundException, ConflictException;

    UserDto getById(Long userId) throws NotFoundException;

    List<UserDto> getAll();

    void delete(Long userId) throws NotFoundException;

    boolean userExists(Long userId);
}