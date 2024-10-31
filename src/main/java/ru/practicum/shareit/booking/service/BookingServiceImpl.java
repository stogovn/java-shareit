package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public BookingDto create(Long userId, Booking booking) {
        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();
        if (!Objects.equals(bookerId, userId) && !Objects.equals(ownerId, userId)) {
            throw new ValidationException("Пользователь с ID = " + userId + " не имеет отношение к бронированию");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto update(Long userId, Long bookingId, boolean approved) {
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        Long ownerId = booking.getItem().getOwner().getId();
        if (!Objects.equals(ownerId, userId)) {
            throw new ValidationException("Только владелец вещи может менять статус бронирования");
        }
        booking.setStatus(status);

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public List<BookingDto> getBookings(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь с id =  " + userId + " не найден");
        }
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        bookings = switch (state.toUpperCase()) {
            case "CURRENT" -> bookingRepository.findCurrentBookings(userId, now);
            case "PAST" -> bookingRepository.findPastBookings(userId, now);
            case "FUTURE" -> bookingRepository.findFutureBookings(userId, now);
            case "WAITING" -> bookingRepository.findWaitingBookings(userId);
            case "REJECTED" -> bookingRepository.findRejectedBookings(userId);
            default -> bookingRepository.findAllByUserIdOrderByStartDesc(userId);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state) {
        if (!userRepository.existsById(ownerId)) {
            throw new EntityNotFoundException("Пользователь с id =  " + ownerId + " не найден");
        }
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        bookings = switch (state.toUpperCase()) {
            case "CURRENT" -> bookingRepository.findCurrentBookingsByOwner(ownerId, now);
            case "PAST" -> bookingRepository.findPastBookingsByOwner(ownerId, now);
            case "FUTURE" -> bookingRepository.findFutureBookingsByOwner(ownerId, now);
            case "WAITING" -> bookingRepository.findWaitingBookingsByOwner(ownerId);
            case "REJECTED" -> bookingRepository.findRejectedBookingsByOwner(ownerId);
            default -> bookingRepository.findAllByOwnerIdOrderByStartDesc(ownerId);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }
}
