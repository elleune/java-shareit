package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock private
    CommentRepository commentRepository;
    @Mock private
    ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("owner").email("owner@example.com").build();
        item = Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
    }

    @Test
    void create_ShouldCreateItem() {
        ItemDto itemDto = ItemDto.builder()
                .name("item")
                .description("description")
                .available(true)
                .build();

        when(userService.getById(anyLong())).thenReturn(Optional.of(UserMapper.toUserDto(owner)));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto result = itemService.create(1L, itemDto);

        assertNotNull(result);
        assertEquals("item", result.getName());
    }

    @Test
    void search_ShouldReturnItems() {
        when(itemRepository.search(anyString())).thenReturn(List.of(item));

        List<ItemDto> result = itemService.search("text");

        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getId());
    }
}
