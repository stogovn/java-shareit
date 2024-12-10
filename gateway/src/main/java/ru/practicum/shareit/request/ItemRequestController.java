package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.validation.OnCreate;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId) {
        return itemRequestClient.getRequestById(requestId);
    }

    @PostMapping
    @Validated({OnCreate.class})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("==> Gateway: Creating itemRequest: {}", itemRequestDto);
        ResponseEntity<Object> response = itemRequestClient.create(userId, itemRequestDto);
        log.info("<== Gateway: Creating itemRequest: {}", response.getBody());
        return response;
    }
}
