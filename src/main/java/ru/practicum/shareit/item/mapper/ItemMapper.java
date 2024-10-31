package ru.practicum.shareit.item.mapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemBookingsDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemMapper {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setRequest(item.getRequest() != null ? item.getRequest() : null);

        return itemDto;
    }

    public Item dtoToItem(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(EntityNotFoundException::new);
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(user);
        item.setRequest(itemDto.getRequest() != null ? itemDto.getRequest() : null);
        return item;
    }

    public ItemInfoDto toItemListInfoDto(Long userId, Item item) {
        ItemInfoDto infoDto = new ItemInfoDto();
        infoDto.setId(item.getId());
        infoDto.setName(item.getName());
        infoDto.setDescription(item.getDescription());
        infoDto.setAvailable(item.getAvailable());
        // Установка информации о ближайших бронированиях
        if (item.getOwner().getId().equals(userId)) {
            bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), LocalDateTime.now())
                    .ifPresent(nextBooking -> infoDto.setNextBooking(new BookingDateDto(nextBooking.getId(),
                            nextBooking.getStart(), nextBooking.getEnd())));
            bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(item.getId(), LocalDateTime.now())
                    .ifPresent(lastBooking -> infoDto.setLastBooking(new BookingDateDto(lastBooking.getId(),
                            lastBooking.getStart(), lastBooking.getEnd())));
        }
        // Добавление комментариев
        List<Comment> comments = commentRepository.findByItemId(item.getId());
        infoDto.setComments(comments.stream().map(commentMapper::toCommentDto).toList());
        return infoDto;
    }

    public ItemBookingsDto toItemInfoDto(Item item) {
        ItemBookingsDto itemBookingsDto = new ItemBookingsDto();
        itemBookingsDto.setId(item.getId());
        itemBookingsDto.setName(item.getName());
        itemBookingsDto.setDescription(item.getDescription());
        itemBookingsDto.setAvailable(item.getAvailable());

        // Установка всех бронирований для данной вещи
        List<Booking> bookings = bookingRepository.findByItemId(item.getId());
        List<BookingDateDto> bookingDateDtos = bookings.stream()
                .map(b -> new BookingDateDto(b.getId(), b.getStart(), b.getEnd()))
                .toList();
        itemBookingsDto.setBookings(bookingDateDtos);
        return itemBookingsDto;
    }
}
