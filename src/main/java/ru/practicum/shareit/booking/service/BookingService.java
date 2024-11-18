package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingCreateDto bookingDto);

    BookingDto getBookingById(Long userId, Long bookingId);

    BookingDto update(Long userId, Long bookingId, boolean approved);

    List<BookingDto> getBookings(Long userId, String state);

    List<BookingDto> getOwnerBookings(Long ownerId, String state);
}
