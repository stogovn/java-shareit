package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
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
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public BookingDto create(Long userId, BookingCreateDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("User cannot book their own item");
        }
        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new ValidationException("Item is not available for booking");
        }
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        List<Booking> conflictingBookings = bookingRepository.findByItemIdAndStartBeforeAndEndAfter(
                item.getId(), end, start);
        if (!conflictingBookings.isEmpty()) {
            throw new ValidationException("The item is already booked during the selected time period");
        }
        Booking booking = bookingMapper.toBooking(bookingDto, booker, item);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndBookerIdOrItemOwnerId(bookingId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

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
            case "PAST" -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case "FUTURE" -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case "WAITING" -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
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
            case "PAST" -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now);
            case "FUTURE" -> bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now);
            case "WAITING" -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
            default -> bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }
}
