package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void testGetUserRequests_Success() throws Exception {
        Long userId = 1L;
        List<ItemsRequestDto> mockResponse = Collections.singletonList(new ItemsRequestDto());

        // Настройка мока для возвращения предопределенного ответа
        when(itemRequestService.getUserRequests(userId)).thenReturn(mockResponse);

        // Выполнение запроса и проверка ответа
        mockMvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(mockResponse.size())); // Проверка размера ответа
    }

    @Test
    void testGetRequestById_Success() throws Exception {
        Long requestId = 1L;
        ItemsRequestDto mockResponse = new ItemsRequestDto();

        // Настройка мока для возвращения предопределенного ответа
        when(itemRequestService.getRequestById(requestId)).thenReturn(mockResponse);

        // Выполнение запроса и проверка ответа
        mockMvc.perform(MockMvcRequestBuilders.get("/requests/{requestId}", requestId))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateItemRequest_Success() throws Exception {
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L); // Настройка данных DTO для теста

        // Настройка мока для возвращения предопределенного ответа
        when(itemRequestService.create(userId, requestDto)).thenReturn(requestDto);

        // Выполнение POST-запроса и проверка ответа
        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestDto.getId()));
    }

    @Test
    void testGetUserRequests_EmptyList() throws Exception {
        Long userId = 1L;
        // Настройка мока для возвращения пустого списка
        when(itemRequestService.getUserRequests(userId)).thenReturn(Collections.emptyList());

        // Выполнение запроса и проверка, что возвращается статус 200 и пустой массив
        mockMvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0)); // Проверка, что размер списка равен 0
    }

    @Test
    void testGetRequestById_NotFound() throws Exception {
        Long requestId = 1L;

        // Настройка мока для выбрасывания исключения при вызове метода
        when(itemRequestService.getRequestById(requestId)).thenThrow(new EntityNotFoundException("Request not found"));

        // Проверка, что возвращается статус 404 при вызове с несуществующим ID
        mockMvc.perform(MockMvcRequestBuilders.get("/requests/{requestId}", requestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Request not found"));
    }

}