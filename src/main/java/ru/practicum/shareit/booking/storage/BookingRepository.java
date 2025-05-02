package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime dateTime);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime dateTime);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long userId);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime dateTime);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime dateTime);

    List<Booking> findByItemOwnerId(Long ownerId);

    Booking findFirstByItemIdAndBookerIdAndEndBeforeOrderByStartDesc(Long itemId, Long userId, LocalDateTime dateTime);

    Booking findFirstByItemIdAndEndBeforeOrderByStartDesc(Long itemId, LocalDateTime now);

    boolean existsByItemIdAndStatusNotAndStartLessThanEqualAndEndGreaterThanEqual(
            Long itemId, BookingStatus status, LocalDateTime end, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);


    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(
            Long bookerId,
            Long itemId,
            LocalDateTime end,
            BookingStatus status);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithBooker(@Param("bookingId") Long bookingId);

    Optional<Booking> findFirstByItemIdAndEndBeforeAndStatusOrderByStartDesc(
            Long itemId,
            LocalDateTime end,
            BookingStatus status
    );


    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId,
            LocalDateTime start,
            BookingStatus status
    );

    Optional<Booking> findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId,
            LocalDateTime start,
            BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start < :now AND b.status = 'APPROVED' ORDER BY b.start DESC LIMIT 1")
    Optional<Booking> findLastBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

}

