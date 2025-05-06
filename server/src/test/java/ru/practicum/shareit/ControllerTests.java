package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BookingController.class, ItemController.class, UserController.class, ItemRequestController.class})
class ControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private ItemService itemService;
    @MockBean
    private UserService userService;
    @MockBean
    private ItemRequestService itemRequestService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testCreateBooking() throws Exception {
        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        BookingOutDto bookingOutDto = BookingOutDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.create(any(), anyLong())).thenReturn(bookingOutDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isCreated())
                .andExpect((ResultMatcher) jsonPath("$.id").value(1L));
    }

    @Test
    void testGetItemById() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("item")
                .build();

        when(itemService.getById(anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.name").value("item"));
    }
}