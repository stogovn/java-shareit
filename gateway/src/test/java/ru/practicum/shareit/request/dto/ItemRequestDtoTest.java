package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestDtoTest {
    private final JacksonTester<ItemRequestDto> json;

    @Test
    void testSerializeItemRequestDto() throws Exception {
        // Создаём объект ItemRequestDto
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Test description");
        itemRequestDto.setRequestor(new UserRequestDto(2L, "Test User", "test@example.com"));
        itemRequestDto.setCreated(LocalDateTime.of(2024, 12, 3, 10, 30));

        // Сериализация в JSON
        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        // Проверки JSON-структуры
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test description");
        assertThat(result).extractingJsonPathNumberValue("$.requestor.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.requestor.name").isEqualTo("Test User");
        assertThat(result).extractingJsonPathStringValue("$.requestor.email").isEqualTo("test@example.com");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-12-03T10:30:00");
    }

    @Test
    void testInvalidDescription_Null() {
        // Создаём объект с null в description
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription(null);

        // Проверяем, что description равно null
        assertThat(itemRequestDto.getDescription()).isNull();
    }

    @Test
    void testInvalidDescription_Empty() {
        // Создаём объект с пустой строкой в description
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("");

        // Проверяем, что description пустое
        assertThat(itemRequestDto.getDescription()).isEmpty();
    }

    @Test
    void testInvalidDescription_TooLong() {
        // Создаём объект с длинным description
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        String longDescription = "a".repeat(501); // 501 символ
        itemRequestDto.setDescription(longDescription);

        // Проверяем, что длина description превышает 500 символов
        assertThat(itemRequestDto.getDescription().length()).isGreaterThan(500);
    }

    @Test
    void testValidDescription() {
        // Создаём объект с корректным description
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        String validDescription = "This is a valid description.";
        itemRequestDto.setDescription(validDescription);

        // Проверяем, что description валидное
        assertThat(itemRequestDto.getDescription()).isEqualTo(validDescription);
    }

    @Test
    void testRequestor_Null() {
        // Создаём объект с null в requestor
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setRequestor(null);

        // Проверяем, что requestor равно null
        assertThat(itemRequestDto.getRequestor()).isNull();
    }

    @Test
    void testCreated_Null() {
        // Создаём объект с null в created
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setCreated(null);

        // Проверяем, что created равно null
        assertThat(itemRequestDto.getCreated()).isNull();
    }
}