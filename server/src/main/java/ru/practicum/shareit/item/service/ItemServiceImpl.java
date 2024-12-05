package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemBookingsDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        checkUserId(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Item item = itemMapper.dtoToItem(itemDto, user);
        item.setOwner(user);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository
                    .findById(itemDto.getRequestId())
                    .orElseThrow(EntityNotFoundException::new);
            item.setRequest(itemRequest);
        }
        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long id, ItemDto itemDto) {
        checkUserId(userId);
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ValidationException("Данный пользователь не может редактировать эту вещь");
        }
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        Item updateItem = itemRepository.save(existingItem);
        return itemMapper.toItemDto(updateItem);
    }

    @Override
    public Collection<ItemBookingsDto> getAllItemsForUser(Long userId) {
        // Получаем все вещи пользователя
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of(); // Если нет вещей, возвращаем пустую коллекцию
        }

        // Извлекаем все бронирования для этих вещей одним запросом
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Booking> bookings = bookingRepository.findByItemIdIn(itemIds);

        // Группируем бронирования по itemId
        Map<Long, List<Booking>> bookingsByItemId = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        // Формируем результат
        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), List.of());
                    List<BookingDateDto> bookingDtos = itemBookings.stream()
                            .map(b -> new BookingDateDto(b.getId(), b.getStart(), b.getEnd()))
                            .toList();
                    return itemMapper.toItemInfoDto(item, bookingDtos);
                })
                .toList();
    }

    @Override
    public ItemInfoDto getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(commentMapper::toCommentDto)
                .toList();

        ItemInfoDto itemInfoDto = itemMapper.toItemListInfoDto(item, commentDtos);

        // Заполнение ближайших бронирований только для владельца
        if (item.getOwner().getId().equals(userId)) {
            bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now())
                    .ifPresent(nextBooking -> itemInfoDto.setNextBooking(new BookingDateDto(nextBooking.getId(),
                            nextBooking.getStart(), nextBooking.getEnd())));

            bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(itemId, LocalDateTime.now())
                    .ifPresent(lastBooking -> itemInfoDto.setLastBooking(new BookingDateDto(lastBooking.getId(),
                            lastBooking.getStart(), lastBooking.getEnd())));
        }

        return itemInfoDto;
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.info("Запрос пустой, возвращаем пустой список");
            return Collections.emptyList();
        }
        return itemRepository.findItemsByText(text).stream()
                .filter(Item::getAvailable)
                .map(itemMapper::toItemDto)
                .toList();
    }

    private void checkUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Указан несуществующий пользователь с id: {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    @Transactional
    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь не найдена"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        LocalDateTime now = LocalDateTime.now();
        boolean hasBooking = bookingRepository.existsByItemIdAndBookerIdAndEndBefore(itemId, userId, now);
        if (!hasBooking) {
            throw new ValidationException("Пользователь не может оставить комментарий, не бронировав товар");
        }
        Comment comment = commentMapper.toComment(commentDto, item, user);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(savedComment);
    }
}
