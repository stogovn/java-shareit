package ru.practicum.shareit.item.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.booking.dto.BookingDateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemBookingsDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface ItemMapper {

    // Преобразование Item в ItemDto
    @Named("toItemDto")
    ItemDto toItemDto(Item item);

    // Преобразование ItemDto в Item
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", source = "itemDto.request")
    Item dtoToItem(ItemDto itemDto, @Context User user);

    // Преобразование Item в ItemInfoDto
    @Mapping(target = "nextBooking", ignore = true) // Будет заполняться вручную
    @Mapping(target = "lastBooking", ignore = true) // Будет заполняться вручную
    @Mapping(target = "comments", source = "comments")
    ItemInfoDto toItemListInfoDto(Item item, List<CommentDto> comments);

    // Преобразование Item в ItemBookingsDto
    @Mapping(target = "bookings", source = "bookingDtos")
    ItemBookingsDto toItemInfoDto(Item item, List<BookingDateDto> bookingDtos);
}
