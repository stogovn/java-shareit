package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, Booking booking);

    BookingDto getBookingById(Long userId, Long bookingId);

    BookingDto update(Long userId, Long bookingId, boolean approved);

    List<BookingDto> getBookings(Long userId, String state);

    List<BookingDto> getOwnerBookings(Long ownerId, String state);
}
