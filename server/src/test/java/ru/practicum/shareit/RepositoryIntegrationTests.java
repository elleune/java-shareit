package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryIntegrationTests {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User user;
    private Item item;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .name("user")
                .email("user@example.com")
                .build());

        item = itemRepository.save(Item.builder()
                .name("item")
                .description("description")
                .available(true)
                .owner(user)
                .build());
    }

    @Test
    void testItemRepository() {
        List<Item> items = itemRepository.findAllByOwnerId(user.getId());

        assertFalse(items.isEmpty());
        assertEquals("item", items.get(0).getName());
    }

    @Test
    void testBookingRepository() {
        Booking booking = bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build());

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(user.getId());

        assertFalse(bookings.isEmpty());
        assertEquals(booking.getId(), bookings.get(0).getId());
    }
}