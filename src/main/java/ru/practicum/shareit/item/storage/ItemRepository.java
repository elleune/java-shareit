package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long itemId);

    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> search(String text);

    void deleteById(Long itemId);
}