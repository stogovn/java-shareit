package ru.practicum.shareit.user.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserRequestDtoTest {
    private final JacksonTester<UserRequestDto> json;

    @Test
    void testSerializeUserRequestDto() throws Exception {
        // Создание объекта UserRequestDto
        UserRequestDto userRequestDto = new UserRequestDto(
                1L,
                "Test User",
                "test@example.com"
        );

        // Сериализация в JSON
        JsonContent<UserRequestDto> result = json.write(userRequestDto);

        // Проверки JSON-структуры
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test User");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("test@example.com");
    }

    @Test
    void testInvalidName_Null() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName(null);

        assertThat(userRequestDto.getName()).isNull();
    }

    @Test
    void testInvalidName_Empty() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName("");

        assertThat(userRequestDto.getName()).isEmpty();
    }

    @Test
    void testInvalidName_TooLong() {
        UserRequestDto userRequestDto = new UserRequestDto();
        String longName = "a".repeat(256); // 256 символов
        userRequestDto.setName(longName);

        assertThat(userRequestDto.getName().length()).isGreaterThan(255);
    }

    @Test
    void testInvalidEmail_Null() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail(null);

        assertThat(userRequestDto.getEmail()).isNull();
    }

    @Test
    void testInvalidEmail_Empty() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("");

        assertThat(userRequestDto.getEmail()).isEmpty();
    }

    @Test
    void testInvalidEmail_NotAnEmail() {
        UserRequestDto userRequestDto = new UserRequestDto();
        String invalidEmail = "invalid-email";
        userRequestDto.setEmail(invalidEmail);

        assertThat(userRequestDto.getEmail()).isEqualTo(invalidEmail);
    }

    @Test
    void testValidData() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setId(1L);
        userRequestDto.setName("John Doe");
        userRequestDto.setEmail("john.doe@example.com");

        assertThat(userRequestDto.getId()).isEqualTo(1L);
        assertThat(userRequestDto.getName()).isEqualTo("John Doe");
        assertThat(userRequestDto.getEmail()).isEqualTo("john.doe@example.com");
    }

}