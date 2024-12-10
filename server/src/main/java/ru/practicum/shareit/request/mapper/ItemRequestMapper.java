package ru.practicum.shareit.request.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ItemRequestMapper {

    @Named("toItemRequestDto")
    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "requestor", expression = "java(user)")
    ItemRequest dtoToItemRequest(ItemRequestDto itemRequestDto, @Context User user);

}

