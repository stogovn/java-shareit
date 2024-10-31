package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemBookingsDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.OnCreate;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @GetMapping
    public Collection<ItemBookingsDto> getAllItemsForUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsForUser(userId);
    }

    @GetMapping("/{itemId}")
    public ItemInfoDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable("itemId") Long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @PostMapping
    @Validated({OnCreate.class})
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("==> Creating item: {}", itemDto);
        ItemDto item = itemService.create(userId, itemMapper.dtoToItem(userId, itemDto));
        log.info("<== Creating item: {}", item);
        return item;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable("itemId") Long id,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("==> Updating item with ID: {} for user ID: {}", id, userId);
        ItemDto updatedItem = itemService.update(userId, id, itemMapper.dtoToItem(userId, itemDto));
        log.info("<== Updated item: {}", updatedItem);
        return updatedItem;
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam String text) {
        if (text == null || text.trim().isEmpty()) {
            log.info("Запрос пустой, возвращаем пустой список");
            return Collections.emptyList();
        }
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) {
        return itemService.addComment(itemId, userId, commentDto);
    }
}
