package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDateDto;

import java.util.List;

@Data
public class ItemBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private List<BookingDateDto> bookings;
}
