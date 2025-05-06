package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) throws ValidationException, ConflictException {
        validateUser(userDto);
        checkEmailUniqueness(userDto.getEmail());

        User user = UserMapper.toUser(userDto);
        try {
            User savedUser = userRepository.save(user);
            return UserMapper.toUserDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto userDto) throws NotFoundException, ConflictException {
        User existingUser = getUserOrThrow(userId);

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            checkEmailUniqueness(userDto.getEmail());
        }

        updateUserFields(existingUser, userDto);
        try {
            User updatedUser = userRepository.save(existingUser);
            return UserMapper.toUserDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getById(Long userId) throws NotFoundException {
        return userRepository.findById(userId)
                .map(UserMapper::toUserDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) throws NotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    private User getUserOrThrow(Long userId) throws NotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateUser(UserDto userDto) throws ValidationException {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!userDto.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать @");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
    }

    private void checkEmailUniqueness(String email) throws ConflictException {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new ConflictException("Пользователь с email " + email + " уже существует");
                });
    }

    private void updateUserFields(User user, UserDto userDto) {
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
    }
}