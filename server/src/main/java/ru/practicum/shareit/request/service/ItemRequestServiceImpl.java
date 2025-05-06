package ru.practicum.shareit.request.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        User requester = UserMapper.toUser(userService.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден")));

        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requester);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getAllByRequester(Long userId) {
        userService.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllOtherUsersRequests(Long userId) {
        userService.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        userService.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }
}
