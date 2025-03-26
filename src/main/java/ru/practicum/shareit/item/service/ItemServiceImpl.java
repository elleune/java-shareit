package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) throws NotFoundException, ValidationException {
        if (!userService.userExists(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        validateItemDto(itemDto);

        User owner = new User(userId, null, null);
        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) throws NotFoundException, ForbiddenException {
        if (!userService.userExists(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Редактирование чужой вещи запрещено");
        }

        updateItemFields(existingItem, itemDto);
        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) throws NotFoundException {
        return ItemMapper.toItemDto(
                itemRepository.findById(itemId)
                        .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"))
        );
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) throws NotFoundException {
        if (!userService.userExists(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text.toLowerCase()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void validateItemDto(ItemDto itemDto) throws ValidationException {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Не указана доступность вещи");
        }
    }

    private void updateItemFields(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }
}