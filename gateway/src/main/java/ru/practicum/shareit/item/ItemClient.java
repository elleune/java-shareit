package ru.practicum.shareit.item;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> createItem(long userId, ItemDto itemDto) {
        return post(API_PREFIX, userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, ItemDto itemDto) {
        return patch(API_PREFIX + "/{itemId}", userId, Map.of("itemId", itemId), itemDto);
    }

    public ResponseEntity<Object> getItem(long userId, long itemId) {
        return get(API_PREFIX + "/{itemId}", userId, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> getAllUserItems(long userId) {
        return get(API_PREFIX, userId);
    }

    public ResponseEntity<Object> searchItems(String text) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get(API_PREFIX + "/search", null, parameters);
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentDto commentDto) {
        return post(API_PREFIX + "/{itemId}/comment", userId, Map.of("itemId", itemId), commentDto);
    }
}