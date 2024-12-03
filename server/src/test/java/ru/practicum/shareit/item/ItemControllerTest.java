package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void testGetAllItemsForUser_Success() throws Exception {
        Long userId = 1L;
        List<BookingDateDto> bookings1 = List
                .of(new BookingDateDto(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
        List<BookingDateDto> bookings2 = List
                .of(new BookingDateDto(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2)));
        ItemBookingsDto item1 = new ItemBookingsDto(1L, "Item 1", "Description 1",
                true, bookings1);
        ItemBookingsDto item2 = new ItemBookingsDto(2L, "Item 2", "Description 2",
                false, bookings2);
        List<ItemBookingsDto> items = List.of(item1, item2);

        // Настройка мока для возвращения списка объектов
        when(itemService.getAllItemsForUser(userId)).thenReturn(items);

        // Проверка, что возвращаемый статус 200 и данные корректны
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(items.size()))
                .andExpect(jsonPath("$[0].id").value(item1.getId()))
                .andExpect(jsonPath("$[0].name").value(item1.getName()))
                .andExpect(jsonPath("$[0].description").value(item1.getDescription()))
                .andExpect(jsonPath("$[0].available").value(item1.getAvailable()))
                .andExpect(jsonPath("$[1].id").value(item2.getId()))
                .andExpect(jsonPath("$[1].bookings[0].id").value(bookings2.getFirst().getId()));
    }

    @Test
    void testGetAllItemsForUser_EmptyList() throws Exception {
        Long userId = 1L;

        // Настройка мока для возвращения пустого списка
        when(itemService.getAllItemsForUser(userId)).thenReturn(Collections.emptyList());

        // Проверка, что возвращаемый статус 200 и пустой список
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void testGetAllItemsForUser_ErrorHandling() throws Exception {
        Long userId = 1L;

        // Настройка мока для выбрасывания исключения
        when(itemService.getAllItemsForUser(userId)).thenThrow(new RuntimeException("Error retrieving items"));

        // Проверка, что возвращается статус 500 при ошибке
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetItemById_Success() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        // Пример ответа от сервиса
        ItemInfoDto mockItem = new ItemInfoDto();
        mockItem.setId(itemId);
        mockItem.setName("Test Item");
        mockItem.setDescription("Test Description");

        when(itemService.getItemById(userId, itemId)).thenReturn(mockItem);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void testGetItemById_NotFound() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        when(itemService.getItemById(userId, itemId)).thenThrow(new EntityNotFoundException("Item not found"));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void testGetItemById_MissingHeader() throws Exception {
        Long itemId = 2L;

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetItemById_InvalidId() throws Exception {
        Long userId = 1L;
        Long invalidItemId = -1L;

        when(itemService.getItemById(userId, invalidItemId)).thenThrow(new IllegalArgumentException("Invalid item ID"));

        mockMvc.perform(get("/items/{itemId}", invalidItemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid item ID"));
    }

    @Test
    void testCreateItem_Success() throws Exception {
        Long userId = 1L;
        ItemDto itemDto = new ItemDto(null, "Item Name", "Item Description", true, 1L);
        ItemDto createdItemDto = new ItemDto(1L, "Item Name", "Item Description", true, 1L);

        // Настройка мока
        when(itemService.create(eq(userId), any(ItemDto.class))).thenReturn(createdItemDto);

        // Отправка POST-запроса и проверка ответа
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdItemDto.getId()))
                .andExpect(jsonPath("$.name").value(createdItemDto.getName()))
                .andExpect(jsonPath("$.description").value(createdItemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(createdItemDto.getAvailable()));
    }

    @Test
    void testCreateItem_EntityNotFoundException() throws Exception {
        Long userId = 1L;
        ItemDto itemDto = new ItemDto(null, "Item Name", "Item Description", true, 1L);

        // Настройка мока для выбрасывания исключения
        when(itemService.create(eq(userId), any(ItemDto.class)))
                .thenThrow(new EntityNotFoundException("User not found"));

        // Отправка POST-запроса и проверка ответа
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void testUpdateItem_Success() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Updated Item Name");
        itemDto.setDescription("Updated Description");
        itemDto.setAvailable(true);

        ItemDto updatedItemDto = new ItemDto();
        updatedItemDto.setId(itemId);
        updatedItemDto.setName("Updated Item Name");
        updatedItemDto.setDescription("Updated Description");
        updatedItemDto.setAvailable(true);

        // Настройка мока для сервисного метода
        when(itemService.update(userId, itemId, itemDto)).thenReturn(updatedItemDto);

        // Выполнение PATCH-запроса
        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Item Name\",\"description\":\"Updated Description\",\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated Item Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void testUpdateItem_NotFound() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Updated Item Name");
        itemDto.setDescription("Updated Description");
        itemDto.setAvailable(true);

        // Настройка мока для выбрасывания исключения
        when(itemService.update(userId, itemId, itemDto))
                .thenThrow(new EntityNotFoundException("Item not found"));

        // Выполнение PATCH-запроса
        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Item Name\",\"description\":\"Updated Description\",\"available\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void testSearchItems_Success() throws Exception {
        String searchText = "laptop";
        List<ItemDto> mockItems = List.of(
                new ItemDto(1L, "Laptop", "Gaming laptop", true, null),
                new ItemDto(2L, "Ultrabook", "Lightweight laptop", true, null)
        );

        // Настройка мока для сервисного слоя
        when(itemService.searchItems(searchText)).thenReturn(mockItems);

        // Отправка GET-запроса
        mockMvc.perform(get("/items/search")
                        .param("text", searchText)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Ultrabook"));

        // Проверка вызова метода в сервисе
        verify(itemService, times(1)).searchItems(searchText);
    }

    @Test
    void testSearchItems_EmptyQuery() throws Exception {
        String emptyQuery = "";

        // Отправка GET-запроса с пустым параметром
        mockMvc.perform(get("/items/search")
                        .param("text", emptyQuery)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        // Проверка, что сервис не был вызван
        verify(itemService, never()).searchItems(anyString());
    }

    @Test
    void testSearchItems_NoResults() throws Exception {
        String searchText = "nonexistent";

        // Настройка мока для пустого ответа
        when(itemService.searchItems(searchText)).thenReturn(Collections.emptyList());

        // Отправка GET-запроса
        mockMvc.perform(get("/items/search")
                        .param("text", searchText)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Проверка вызова метода в сервисе
        verify(itemService, times(1)).searchItems(searchText);
    }

    @Test
    void testAddComment_Success() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;

        CommentDto requestCommentDto = new CommentDto();
        requestCommentDto.setText("Great item!");

        CommentDto responseCommentDto = new CommentDto();
        responseCommentDto.setId(1L);
        responseCommentDto.setText("Great item!");
        responseCommentDto.setAuthorName("John Doe");
        responseCommentDto.setCreated(LocalDateTime.now());

        when(itemService.addComment(eq(itemId), eq(userId), any(CommentDto.class))).thenReturn(responseCommentDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestCommentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseCommentDto.getId()))
                .andExpect(jsonPath("$.text").value(responseCommentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(responseCommentDto.getAuthorName()))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void testAddComment_ItemNotFound() throws Exception {
        Long userId = 1L;
        Long itemId = 999L;

        CommentDto requestCommentDto = new CommentDto();
        requestCommentDto.setText("Great item!");

        when(itemService.addComment(eq(itemId), eq(userId), any(CommentDto.class)))
                .thenThrow(new EntityNotFoundException("Item not found"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestCommentDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

}