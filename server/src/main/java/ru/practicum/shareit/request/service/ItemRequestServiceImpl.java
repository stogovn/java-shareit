package ru.practicum.shareit.request.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper mapper;

    @Transactional
    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        ItemRequest itemRequest = mapper.dtoToItemRequest(itemRequestDto, user);

        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        return mapper.toItemRequestDto(savedItemRequest);
    }

    public List<ItemsRequestDto> getUserRequests(Long userId) {
        // Получаем запросы текущего пользователя
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        // Получаем все вещи, связанные с запросами, за один запрос
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();
        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        // Группируем вещи по requestId
        Map<Long, List<ItemResponseDto>> itemsByRequestId = items.stream()
                .map(item -> {
                    ItemResponseDto responseDto = new ItemResponseDto();
                    responseDto.setId(item.getId());
                    responseDto.setName(item.getName());
                    responseDto.setOwnerId(item.getOwner().getId());
                    return Map.entry(item.getRequest().getId(), responseDto);
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        // Маппим запросы с данными об ответах
        return requests.stream()
                .map(request -> {
                    ItemsRequestDto dto = new ItemsRequestDto();
                    dto.setId(request.getId());
                    dto.setDescription(request.getDescription());
                    dto.setCreated(request.getCreated());
                    dto.setItems(itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList()));
                    return dto;
                }).toList();
    }

    public ItemsRequestDto getRequestById(Long requestId) {
        // Получаем запрос по ID
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        // Получаем все вещи, относящиеся к данному запросу, одним запросом
        List<Item> items = itemRepository.findByRequestId(requestId);

        // Преобразуем вещи в DTO
        List<ItemResponseDto> itemResponses = items.stream()
                .map(item -> {
                    ItemResponseDto responseDto = new ItemResponseDto();
                    responseDto.setId(item.getId());
                    responseDto.setName(item.getName());
                    responseDto.setOwnerId(item.getOwner().getId());
                    return responseDto;
                })
                .toList();

        // Создаем и заполняем ItemsRequestDto
        ItemsRequestDto itemsRequestDto = new ItemsRequestDto();
        itemsRequestDto.setId(request.getId());
        itemsRequestDto.setDescription(request.getDescription());
        itemsRequestDto.setCreated(request.getCreated());
        itemsRequestDto.setItems(itemResponses);

        return itemsRequestDto;
    }

}
