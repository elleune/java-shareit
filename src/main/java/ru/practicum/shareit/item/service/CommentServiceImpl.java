package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public CommentDto create(Long userId, Long itemId, CommentDto commentDto) {
        User author = UserMapper.toUser(userService.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemService.getItemById(itemId);

        // Проверка 1: Пользователь должен был арендовать вещь в прошлом
        boolean hasPastBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED);
        
        // Проверка 2: У вещи не должно быть активных бронирований
        boolean hasActiveBooking = bookingRepository.existsByItemIdAndEndAfterAndStatus(
                itemId, LocalDateTime.now(), BookingStatus.APPROVED);

        if (!hasPastBooking) {
            throw new ValidationException("Вы не можете оставить отзыв на эту вещь");
        }
        
        if (hasActiveBooking) {
            throw new ValidationException("Нельзя оставить отзыв: у вещи есть активное бронирование");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentRepository.findByItemIdWithAuthor(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
