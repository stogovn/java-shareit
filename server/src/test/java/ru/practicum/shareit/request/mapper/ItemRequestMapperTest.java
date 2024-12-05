package ru.practicum.shareit.request.mapper;


import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import static org.junit.jupiter.api.Assertions.assertNull;

class ItemRequestMapperTest {
    private final ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();

    @Test
    void testToItemRequestDto_NullInput() {
        // Передаем null в метод toItemRequestDto
        ItemRequestDto result = itemRequestMapper.toItemRequestDto(null);

        // Проверяем, что результат null
        assertNull(result, "Метод toItemRequestDto должен вернуть null, если входной параметр null");
    }

    @Test
    void testDtoToItemRequest_NullInput() {
        // Передаем null в метод dtoToItemRequest
        ItemRequest result = itemRequestMapper.dtoToItemRequest(null, null);

        // Проверяем, что результат null
        assertNull(result, "Метод dtoToItemRequest должен вернуть null, если входной параметр null");
    }
}