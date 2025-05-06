package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;


import java.util.Map;


@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> createRequest(long userId, ItemRequestCreateDto itemRequestDto) {
        return post(API_PREFIX, userId, itemRequestDto);
    }

    public ResponseEntity<Object> getAllUserRequests(long userId) {
        return get(API_PREFIX, userId);
    }

    public ResponseEntity<Object> getAllOtherRequests(long userId, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get(API_PREFIX + "/all", userId, parameters);
    }

    public ResponseEntity<Object> getRequest(long userId, long requestId) {
        return get(API_PREFIX + "/{requestId}", userId, Map.of("requestId", requestId));
    }
}
