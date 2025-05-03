package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public ItemDto getById(Long itemId, Long userId) throws NotFoundException {
        Item item = getItemOrThrow(itemId);
        BookingOutDto lastBooking = null;
        BookingOutDto nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = bookingRepository
                    .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                            itemId,
                            LocalDateTime.now(),
                            BookingStatus.APPROVED)
                    .map(BookingMapper::toBookingOutDto)
                    .orElse(null);

            nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            itemId,
                            LocalDateTime.now(),
                            BookingStatus.APPROVED)
                    .map(BookingMapper::toBookingOutDto)
                    .orElse(null);
        }

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
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> allBookings = bookingRepository.findByItemIdIn(itemIds);
        Map<Long, List<Booking>> bookingsByItemId = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        List<Comment> allComments = commentRepository.findByItemIdIn(itemIds);
        Map<Long, List<CommentDto>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(comment -> {
                            CommentDto dto = CommentMapper.toCommentDto(comment);
                            dto.setAuthorName(comment.getAuthor().getName());
                            return dto;
                        }, Collectors.toList())
                ));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.toItemDto(item);

                    List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList());
                    if (!itemBookings.isEmpty()) {
                        // Находим последнее завершенное бронирование
                        Optional<Booking> lastBooking = itemBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                                .filter(b -> b.getEnd().isBefore(now))
                                .max(Comparator.comparing(Booking::getStart));

                        Optional<Booking> nextBooking = itemBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                                .filter(b -> b.getStart().isAfter(now))
                                .min(Comparator.comparing(Booking::getStart));

                        lastBooking.ifPresent(booking ->
                                itemDto.setLastBooking(BookingMapper.toBookingOutDto(booking)));
                        nextBooking.ifPresent(booking ->
                                itemDto.setNextBooking(BookingMapper.toBookingOutDto(booking)));
                    }

                    itemDto.setComments(commentsByItemId.getOrDefault(item.getId(), Collections.emptyList()));

                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = UserMapper.toUser(userService.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"))
        );

        Item item = getItemById(itemId);

        if (!bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED)) {
            throw new ValidationException("Вы не можете оставить отзыв на эту вещь");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
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
