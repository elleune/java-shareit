package ru.practicum.shareit.request;

/**
 * TODO Sprint add-item-requests.
 */

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ItemRequest {
    private Long id;
    private String description;
    private Long requesterId;
    private LocalDateTime created;
}