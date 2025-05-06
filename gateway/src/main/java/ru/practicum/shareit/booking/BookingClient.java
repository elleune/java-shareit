package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> createBooking(long userId, BookingInDto bookingInDto) {
        return post(API_PREFIX, userId, bookingInDto);
    }

    public ResponseEntity<Object> updateBooking(long userId, long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );
        return patch(API_PREFIX + "/{bookingId}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBooking(long userId, long bookingId) {
        return get(API_PREFIX + "/{bookingId}", userId, Map.of("bookingId", bookingId));
    }

    public ResponseEntity<Object> getAllBookings(long userId, String state) {
        Map<String, Object> parameters = Map.of(
                "state", state
        );
        return get(API_PREFIX, userId, parameters);
    }

    public ResponseEntity<Object> getAllOwnerBookings(long userId, String state) {
        Map<String, Object> parameters = Map.of(
                "state", state
        );
        return get(API_PREFIX + "/owner", userId, parameters);
    }
}
