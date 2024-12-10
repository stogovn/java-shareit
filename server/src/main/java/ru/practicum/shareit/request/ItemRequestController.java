package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.validation.OnCreate;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping
    public List<ItemsRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemsRequestDto getRequestById(@PathVariable Long requestId) {
        return itemRequestService.getRequestById(requestId);
    }

    @PostMapping
    @Validated({OnCreate.class})
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody ItemRequestDto itemRequestDto) {
        log.info("==> Creating itemRequest: {}", itemRequestDto);
        ItemRequestDto itemRequest = itemRequestService.create(userId, itemRequestDto);
        log.info("<== Creating itemRequest: {}", itemRequest);
        return itemRequest;
    }
}
