package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.OnCreate;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotNull(message = "ItemId обязателен", groups = OnCreate.class)
    private Long itemId;
    @JsonProperty("item")
    private ItemDto item;
    @JsonProperty("booker")
    private UserDto booker;
    @NotNull(message = "Дата начала бронирования обязательна", groups = OnCreate.class)
    @Future(message = "Дата начала бронирования не может быть в прошлом", groups = OnCreate.class)
    private LocalDateTime start;
    @NotNull(message = "Дата начала бронирования обязательна", groups = OnCreate.class)
    @Future(message = "Дата окончания бронирования не может быть в прошлом", groups = OnCreate.class)
    private LocalDateTime end;
    private BookingStatus status;

    @AssertTrue(message = "Дата окончания бронирования должна быть позже даты начала")
    @JsonIgnore
    public boolean isEndAfterStart() {
        return end.isAfter(start);
    }
}
