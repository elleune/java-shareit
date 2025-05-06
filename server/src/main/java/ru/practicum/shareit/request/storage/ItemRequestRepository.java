package ru.practicum.shareit.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long requesterId);

    List<ItemRequest> findAllByRequesterIdNotOrderByCreatedDesc(Long requesterId);

    Optional<ItemRequest> findByIdAndRequesterId(Long id, Long requesterId);
}