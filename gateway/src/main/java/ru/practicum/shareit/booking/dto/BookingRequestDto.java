package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import java.time.LocalDateTime;

@Data
public class BookingRequestDto {
    @NotNull(message = "ItemId обязателен", groups = OnCreate.class)
    private Long itemId;
    @NotNull(message = "Дата начала бронирования обязательна", groups = OnCreate.class)
    @Future(message = "Дата начала бронирования не может быть в прошлом", groups = OnCreate.class)
    private LocalDateTime start;
    @NotNull(message = "Дата начала бронирования обязательна", groups = OnCreate.class)
    @Future(message = "Дата окончания бронирования не может быть в прошлом", groups = OnCreate.class)
    private LocalDateTime end;

    @AssertTrue(message = "Дата окончания бронирования должна быть позже даты начала")
    @JsonIgnore
    public boolean isEndAfterStart() {
        return end.isAfter(start);
    }
}
