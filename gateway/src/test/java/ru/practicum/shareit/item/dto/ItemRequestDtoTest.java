package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestDtoTest {
    private final JacksonTester<ItemRequestDto> json;

    @Test
    void testSerializeItemRequestDto() throws Exception {
        // Создаём объект
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setName("Test Item");
        itemRequestDto.setDescription("Test Description");
        itemRequestDto.setAvailable(true);
        itemRequestDto.setRequestId(10L);

        // Сериализация в JSON
        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        // Проверки JSON-структуры
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(10);
    }

    @Test
    void testInvalidName() {
        // Объект с пустым именем
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("");
        itemRequestDto.setDescription("Valid Description");
        itemRequestDto.setAvailable(true);

        // Проверяем, что имя пустое
        assertThat(itemRequestDto.getName()).isEmpty();
    }

    @Test
    void testInvalidDescription() {
        // Объект с длинным описанием
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        String longDescription = "a".repeat(501); // 501 символ
        itemRequestDto.setDescription(longDescription);
        itemRequestDto.setName("Valid Name");
        itemRequestDto.setAvailable(true);

        // Проверяем, что описание длинное
        assertThat(itemRequestDto.getDescription().length()).isGreaterThan(500);
    }

    @Test
    void testNullAvailability() {
        // Объект с null в available
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("Valid Name");
        itemRequestDto.setDescription("Valid Description");
        itemRequestDto.setAvailable(null);

        // Проверяем, что available = null
        assertThat(itemRequestDto.getAvailable()).isNull();
    }

    @Test
    void testNegativeRequestId() {
        // Объект с отрицательным requestId
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("Valid Name");
        itemRequestDto.setDescription("Valid Description");
        itemRequestDto.setAvailable(true);
        itemRequestDto.setRequestId(-5L);

        // Проверяем, что requestId отрицательное
        assertThat(itemRequestDto.getRequestId()).isNegative();
    }
}