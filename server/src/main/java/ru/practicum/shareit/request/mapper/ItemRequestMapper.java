package ru.practicum.shareit.request.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring", uses = {CommentMapper.class, ItemMapper.class})
public interface ItemRequestMapper {

    @Named("toItemRequestDto")
    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "requestor", source = "itemRequestDto.requestor")
    ItemRequest dtoToItemRequest(ItemRequestDto itemRequestDto, @Context User user);

}

