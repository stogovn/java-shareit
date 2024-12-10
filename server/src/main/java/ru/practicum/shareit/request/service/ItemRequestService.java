package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    List<ItemsRequestDto> getUserRequests(Long userId);

    ItemsRequestDto getRequestById(Long requestId);
}
