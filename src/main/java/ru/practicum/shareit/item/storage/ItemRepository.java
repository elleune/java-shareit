package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
            SELECT i FROM Item i
            WHERE i.available = true
            AND lower(i.name) LIKE lower(%?1%) OR lower(i.description) LIKE lower(%?1%)
            """)
    List<Item> findByText(String text);

    List<Item> findAllByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true")
    List<Item> search(@Param("text") String text);

    @Query("SELECT i FROM Item i JOIN FETCH i.comments WHERE i.id = :itemId")
    Optional<Item> findByIdWithComments(@Param("itemId") Long itemId);

    List<Item> findByOwnerId(Long ownerId);
}