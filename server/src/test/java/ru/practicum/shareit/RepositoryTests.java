package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RepositoryTests {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;

    @Test
    void testFindByEmail() {
        User user = User.builder()
                .name("name")
                .email("email@example.com")
                .build();
        entityManager.persist(user);

        Optional<User> found = userRepository.findByEmail("email@example.com");

        assertTrue(found.isPresent());
        assertEquals("name", found.get().getName());
    }

    @Test
    void testFindAllByOwnerId() {
        User owner = User.builder()
                .name("owner")
                .email("owner@example.com")
                .build();
        entityManager.persist(owner);

        Item item = Item.builder()
                .name("item")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(item);

        List<Item> items = itemRepository.findAllByOwnerId(owner.getId());

        assertFalse(items.isEmpty());
        assertEquals("item", items.get(0).getName());
    }
}
