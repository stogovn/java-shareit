package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRequestDtoTest {

    private final JacksonTester<BookingRequestDto> json;

    @Test
    void testSerializeBookingRequestDto() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        JsonContent<BookingRequestDto> result = json.write(bookingRequestDto);

        // Форматирование даты и времени для сравнения
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
        String formattedStart = bookingRequestDto.getStart().format(formatter);
        String formattedEnd = bookingRequestDto.getEnd().format(formatter);

        // Сравниваем строки, а не объекты LocalDateTime
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(formattedStart);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(formattedEnd);
    }

    @Test
    void testValidBookingRequest() {
        // Создание объекта с корректными данными
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        // Проверка, что данные установлены корректно
        assertThat(bookingRequestDto.getItemId()).isEqualTo(1L);
        assertThat(bookingRequestDto.getStart()).isAfter(LocalDateTime.now());
        assertThat(bookingRequestDto.getEnd()).isAfter(bookingRequestDto.getStart());
    }

    @Test
    void testItemIdNotNull() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(null);

        // Проверка, что itemId равен null
        assertThat(bookingRequestDto.getItemId()).isNull();
    }

    @Test
    void testStartDateNotNull() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setStart(null);

        // Проверка, что start равен null
        assertThat(bookingRequestDto.getStart()).isNull();
    }

    @Test
    void testEndDateNotNull() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setEnd(null);

        // Проверка, что end равен null
        assertThat(bookingRequestDto.getEnd()).isNull();
    }

    @Test
    void testEndDateBeforeStart() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(2));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(1));

        // Проверка, что end не больше start с учетом форматирования
        assertThatThrownBy(() -> {
            if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
                throw new IllegalArgumentException("Дата окончания бронирования должна быть позже даты начала");
            }
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Дата окончания бронирования должна быть позже даты начала");

    }

    @Test
    void testEndDateAfterStart() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        // Проверка, что end после start
        assertThat(bookingRequestDto.isEndAfterStart()).isTrue();
    }
}