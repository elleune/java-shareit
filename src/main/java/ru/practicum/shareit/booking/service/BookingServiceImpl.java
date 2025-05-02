package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingOutDto create(BookingInDto bookingInDto, Long userId) {
        validateBookingDates(bookingInDto);

        User booker = getUserOrThrow(userId);
        ItemDto itemDto = itemService.getById(bookingInDto.getItemId());
        Item item = itemRepository.findById(bookingInDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (bookingRepository.existsByItemIdAndStatusNotAndStartLessThanEqualAndEndGreaterThanEqual(
                bookingInDto.getItemId(),
                BookingStatus.REJECTED,
                bookingInDto.getEnd(),
                bookingInDto.getStart())) {
            throw new ValidationException("Вещь уже забронирована на указанные даты");
        }

        Booking booking = BookingMapper.toBooking(bookingInDto, booker, item);
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingOutDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingOutDto update(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтверждать бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже было подтверждено или отклонено");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingOutDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingOutDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Просматривать бронирование могут только автор или владелец");
        }

        if (booking.getStatus() != BookingStatus.APPROVED &&
                booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование не подтверждено");
        }

        return BookingMapper.toBookingOutDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutDto> getAllOfUserByState(Long userId, BookingState state) {
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED);
                break;
            case CANCELLED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.CANCELLED);
                break;
            default: // ALL
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingOutDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutDto> getAllOfOwnerByState(Long userId, BookingState state) {
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED);
                break;
            case CANCELLED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.CANCELLED);
                break;
            default:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingOutDto)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        UserDto userDto = userService.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        return UserMapper.toUser(userDto);
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено"));
    }

    private void validateBookingDates(BookingInDto bookingInDto) {
        if (bookingInDto.getStart() == null || bookingInDto.getEnd() == null) {
            throw new ValidationException("Даты начала и окончания бронирования обязательны");
        }

        if (bookingInDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }

        if (bookingInDto.getEnd().isBefore(bookingInDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше даты начала");
        }

        if (bookingInDto.getStart().equals(bookingInDto.getEnd())) {
            throw new ValidationException("Даты начала и окончания бронирования не могут совпадать");
        }
    }
}
