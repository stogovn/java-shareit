package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.validation.OnCreate;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getAllItemsForUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getAllItemsForUser(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable("itemId") Long itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @PostMapping
    @Validated({OnCreate.class})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody @Valid ItemRequestDto itemDto) {
        log.info("==> Gateway: Creating item: {}", itemDto);

        // Перенаправляем запрос на сервер
        ResponseEntity<Object> response = itemClient.create(userId, itemDto);

        log.info("<== Gateway: Item created: {}", response.getBody());
        return response;
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable("itemId") Long id,
                                         @RequestBody @Valid ItemRequestDto itemDto) {
        log.info("==> Gateway: Updating item: {}", itemDto);

        // Перенаправляем запрос на сервер
        ResponseEntity<Object> response = itemClient.update(userId, id, itemDto);

        log.info("<== Gateway: Item updated: {}", response.getBody());
        return response;
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {

        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Valid CommentRequestDto commentDto) {
        log.info("==> Gateway: Add comment: {}", commentDto);
        ResponseEntity<Object> response = itemClient.addComment(userId, itemId, commentDto);
        log.info("<== Gateway: Add comment: {}", response.getBody());
        return response;
    }
}
