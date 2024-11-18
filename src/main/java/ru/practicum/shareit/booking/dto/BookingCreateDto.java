package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.validation.OnCreate;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreateDto {

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

