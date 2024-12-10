package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {
    private final BookingMapper bookingMapper = new BookingMapperImpl();

    @Test
    void testToBookingDto_NullBooking() {
        // Передаем null в метод
        BookingDto result = bookingMapper.toBookingDto(null);

        // Проверяем, что результат null
        assertNull(result, "Метод toBookingDto должен вернуть null, если входной параметр booking равен null");
    }

    @Test
    void testToBooking_NullBookingDtoAndUserAndItem() {
        // Передаем null для bookingDto, user и item
        Booking result = bookingMapper.toBooking(null, null, null);

        // Проверяем, что результат null
        assertNull(result, "Метод toBooking должен вернуть null");
    }
}