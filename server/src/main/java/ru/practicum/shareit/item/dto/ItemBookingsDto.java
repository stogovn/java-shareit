package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDateDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private List<BookingDateDto> bookings;
}
