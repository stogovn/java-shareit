package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(Long userId, BookingRequestDto bookingDto) {
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> update(Long userId, Long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of(
                "approved", approved);
        String uri = UriComponentsBuilder.fromPath("/" + bookingId)
                .queryParam("approved", approved)
                .build()
                .toUriString();
        return patch(uri, userId, parameters);
    }

    public ResponseEntity<Object> getBookings(Long userId, String state) {
        Map<String, Object> parameters = Map.of(
                "state", state);
        return get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, String state) {
        Map<String, Object> parameters = Map.of(
                "state", state);
        return get("/owner?state={state}", userId, parameters);
    }
}
