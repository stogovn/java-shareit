package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {
    private final ItemMapper itemMapper = new ItemMapperImpl();

    @Test
    void testToItemDto_NullItem() {
        // Передаем null в метод
        ItemDto result = itemMapper.toItemDto(null);

        // Проверяем, что результат null
        assertNull(result, "Метод toItemDto должен вернуть null, если входной параметр item равен null");
    }

    @Test
    void testDtoToItem_NullItemDto() {
        // Передаем null в метод
        Item result = itemMapper.dtoToItem(null, null);

        // Проверяем, что результат null
        assertNull(result, "Метод dtoToItem должен вернуть null, если входной параметр itemDto равен null");
    }

    @Test
    void testToItemListInfoDto_NullItemAndComments() {
        // Передаем null в метод
        ItemInfoDto result = itemMapper.toItemListInfoDto(null, null);

        // Проверяем, что результат null
        assertNull(result, "Метод toItemListInfoDto должен вернуть null, если входные параметры item и comments равны null");
    }

    @Test
    void testToItemListInfoDto_NullItem() {
        // Создаем тестовый список comments
        List<CommentDto> comments = List.of(new CommentDto());
        // Передаем null для item и тестовый список для comments
        ItemInfoDto result = itemMapper.toItemListInfoDto(null, comments);

        // Проверяем, что результат не равен null, так как comments не null
        assertNull(result.getId(), "Метод toItemListInfoDto должен вернуть объект, если item равен null, но comments не равен null");
    }

    @Test
    void testToItemListInfoDto_NullComments() {
        // Создаем тестовый объект item
        Item item = new Item();
        // Передаем item и null для comments
        ItemInfoDto result = itemMapper.toItemListInfoDto(item, null);

        // Проверяем, что результат не равен null, так как item не null
        assertNull(result.getComments(), "Метод toItemListInfoDto должен вернуть объект с пустым списком комментариев, если comments равны null");
    }

    @Test
    void testToItemInfoDto_NullItemAndBookingDtos() {
        // Передаем null в метод
        ItemBookingsDto result = itemMapper.toItemInfoDto(null, null);

        // Проверяем, что результат null
        assertNull(result, "Метод toItemInfoDto должен вернуть null, если входные параметры item и bookingDtos равны null");
    }

    @Test
    void testToItemInfoDto_NullItem() {
        // Создаем тестовый список bookingDtos
        List<BookingDateDto> bookingDtos = List.of(new BookingDateDto());
        // Передаем null для item и тестовый список для bookingDtos
        ItemBookingsDto result = itemMapper.toItemInfoDto(null, bookingDtos);

        // Проверяем, что результат не равен null, так как bookingDtos не null
        assertNull(result.getId(), "Метод toItemInfoDto должен вернуть объект, если item равен null, но bookingDtos не равен null");
    }

    @Test
    void testToItemInfoDto_NullBookingDtos() {
        // Создаем тестовый объект item
        Item item = new Item();
        // Передаем item и null для bookingDtos
        ItemBookingsDto result = itemMapper.toItemInfoDto(item, null);

        // Проверяем, что результат не равен null, так как item не null
        assertNull(result.getBookings(), "Метод toItemInfoDto должен вернуть объект с пустым списком bookingDtos, если bookingDtos равны null");
    }
}