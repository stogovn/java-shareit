package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.validation.OnCreate;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingClient bookingClent;

    @PostMapping
    @Validated({OnCreate.class})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody BookingRequestDto bookingDto) {
        log.info("==> Gateway: Creating booking: {}", bookingDto);
        ResponseEntity<Object> response = bookingClent.create(userId, bookingDto);
        log.info("<== Gateway: Creating booking: {}", response.getBody());
        return response;
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable("bookingId") Long bookingId) {
        return bookingClent.getBookingById(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable("bookingId") Long bookingId,
                                         @RequestParam boolean approved) {
        log.info("==> Gateway: Updating booking c id = {} with approval: {}", bookingId, approved);
        ResponseEntity<Object> response = bookingClent.update(userId, bookingId, approved);
        log.info("<== Gateway: Updating booking: {}", response.getBody());
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(value = "state", defaultValue = "ALL") String state) {
        return bookingClent.getBookings(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(value = "state", defaultValue = "ALL") String state) {
        return bookingClent.getOwnerBookings(userId, state);
    }
}
