package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public ItemDto create(Long userId, Item item) {
        checkUserId(userId);
        User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        item.setOwner(user);
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long id, Item item) {
        checkUserId(userId);
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ValidationException("Данный пользователь не может редактировать эту вещь");
        }
        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }
        Item updateItem = itemRepository.save(existingItem);
        return itemMapper.toItemDto(updateItem);
    }

    @Override
    public Collection<ItemBookingsDto> getAllItemsForUser(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return items.stream()
                .map(itemMapper::toItemInfoDto)
                .toList();
    }

    @Override
    public ItemInfoDto getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        return itemMapper.toItemListInfoDto(userId, item);
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
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

        boolean hasBooking = bookingRepository.existsByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now());
        if (!hasBooking) {
            throw new ValidationException("Пользователь не может оставить комментарий, не бронировав товар");
        }
        Comment comment = commentMapper.toComment(commentDto, item, user);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(savedComment);
    }
}
