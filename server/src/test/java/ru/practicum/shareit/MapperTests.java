package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperTests {

    @Test
    void testUserMapper() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email@example.com")
                .build();

        UserDto dto = UserMapper.toUserDto(user);
        User mappedUser = UserMapper.toUser(dto);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), mappedUser.getName());
    }

    @Test
    void testItemMapper() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .owner(owner)
                .build();

        ItemDto dto = ItemMapper.toItemDto(item);
        Item mappedItem = ItemMapper.toItem(dto, owner);

        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), mappedItem.getName());
    }
}