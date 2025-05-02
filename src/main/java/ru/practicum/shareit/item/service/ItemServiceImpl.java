package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    @Override
    public ItemDto create(Long userId, ItemDto itemDto) throws NotFoundException, ValidationException {
        validateItem(itemDto);
        User owner = getUserOrThrow(userId);

        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto)
            throws NotFoundException, ForbiddenException, ValidationException {
        getUserOrThrow(userId);
        Item existingItem = getItemOrThrow(itemId);

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Редактирование чужой вещи запрещено");
        }

        updateItemFields(existingItem, itemDto);
        return ItemMapper.toItemDto(itemRepository.save(existingItem));
    }

    @Override
    public ItemDto getById(Long itemId) throws NotFoundException {
        Item item = getItemOrThrow(itemId);

        BookingOutDto lastBooking = bookingRepository
                .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                        itemId,
                        LocalDateTime.now(),
                        BookingStatus.APPROVED)
                .map(BookingMapper::toBookingOutDto)
                .orElse(null);

        BookingOutDto nextBooking = bookingRepository
                .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                        itemId,
                        LocalDateTime.now(),
                        BookingStatus.APPROVED)
                .map(BookingMapper::toBookingOutDto)
                .orElse(null);

        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(comment -> {
                    CommentDto dto = CommentMapper.toCommentDto(comment);
                    dto.setAuthorName(comment.getAuthor().getName());
                    return dto;
                })
                .collect(Collectors.toList());

        return ItemMapper.toItemDto(item, comments, lastBooking, nextBooking);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) throws NotFoundException {
        getUserOrThrow(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return items.stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.toItemDto(item);
                    addBookingInfo(itemDto, item.getId());
                    itemDto.setComments(commentRepository.findByItemIdWithAuthor(item.getId()).stream()
                            .map(CommentMapper::toCommentDto)
                            .collect(Collectors.toList()));
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        CommentService commentService = new CommentServiceImpl(
                commentRepository,
                userService,
                this,
                bookingRepository
        );
        return commentService.create(userId, itemId, commentDto);
    }

    private void addBookingInfo(ItemDto itemDto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Booking> lastBookingOpt = bookingRepository
                .findFirstByItemIdAndEndBeforeAndStatusOrderByStartDesc(
                        itemId,
                        now,
                        BookingStatus.APPROVED
                );

        Optional<Booking> nextBookingOpt = bookingRepository
                .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                        itemId,
                        now,
                        BookingStatus.APPROVED
                );

        lastBookingOpt.ifPresent(booking ->
                itemDto.setLastBooking(BookingMapper.toBookingOutDto(booking)));
        nextBookingOpt.ifPresent(booking ->
                itemDto.setNextBooking(BookingMapper.toBookingOutDto(booking)));
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
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

    private User getUserOrThrow(Long userId) throws NotFoundException {
        UserDto userDto = userService.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        return UserMapper.toUser(userDto);
    }

    private Item getItemOrThrow(Long itemId) throws NotFoundException {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    private void validateItem(ItemDto itemDto) throws ValidationException {
        if (itemDto == null) {
            throw new ValidationException("Данные вещи не могут быть null");
        }
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

    private void updateItemFields(Item item, ItemDto itemDto) throws ValidationException {
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException("Название вещи не может быть пустым");
            }
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException("Описание вещи не может быть пустым");
            }
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }
}
