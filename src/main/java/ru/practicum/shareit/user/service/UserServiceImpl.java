package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) throws ValidationException, ConflictException {
        validateUser(userDto);
        checkEmailUniqueness(userDto.getEmail());

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) throws NotFoundException, ConflictException {
        User existingUser = getUserOrThrow(userId);

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            checkEmailUniqueness(userDto.getEmail());
        }

        updateUserFields(existingUser, userDto);
        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public Optional<UserDto> getById(Long userId) throws NotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        return Optional.of(UserMapper.toUserDto(user));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) throws NotFoundException {
        getUserOrThrow(userId);
        userRepository.delete(userId);
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.findById(userId).isPresent();
    }

    private User getUserOrThrow(Long userId) throws NotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateUser(UserDto userDto) {
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