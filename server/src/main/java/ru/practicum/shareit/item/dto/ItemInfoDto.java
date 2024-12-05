package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDateDto;

import java.util.List;

@Data
public class ItemInfoDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDateDto nextBooking; // ближайшее бронирование
    private BookingDateDto lastBooking; // последнее бронирование
    private List<CommentDto> comments;
}
