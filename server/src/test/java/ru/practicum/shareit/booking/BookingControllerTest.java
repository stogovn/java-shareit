package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void testCreateBooking_Success() throws Exception {
        Long userId = 1L;

        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(2L);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(3L);
        bookingDto.setItemId(2L);
        bookingDto.setStart(bookingCreateDto.getStart());
        bookingDto.setEnd(bookingCreateDto.getEnd());
        bookingDto.setStatus(BookingStatus.WAITING);

        when(bookingService.create(eq(userId), any(BookingCreateDto.class))).thenReturn(bookingDto);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.itemId").value(bookingDto.getItemId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));
    }

    @Test
    void testCreateBooking_ItemNotFound() throws Exception {
        Long userId = 1L;

        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(2L);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.create(eq(userId), any(BookingCreateDto.class)))
                .thenThrow(new EntityNotFoundException("Item not found"));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void testCreateBooking_UserCannotBookOwnItem() throws Exception {
        Long userId = 1L;

        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(2L);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.create(eq(userId), any(BookingCreateDto.class)))
                .thenThrow(new ValidationException("User cannot book their own item"));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User cannot book their own item"));
    }

    @Test
    void testGetBookingById_Success() throws Exception {
        // Подготовка тестового объекта
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setItemId(2L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setStatus(BookingStatus.WAITING);

        // Настройка поведения мока
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(bookingDto);

        Long userId = 1L;
        Long bookingId = 1L;
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .optionalEnd()
                .toFormatter();
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.itemId").value(bookingDto.getItemId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()))
                .andExpect(jsonPath("$.start").value(formatter.format(bookingDto.getStart())))
                .andExpect(jsonPath("$.end").value(formatter.format(bookingDto.getEnd())));
    }

    @Test
    void testUpdateBooking_Success() throws Exception {
        // Подготовка тестового объекта
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setItemId(2L);
        bookingDto.setStatus(BookingStatus.APPROVED);

        // Настройка поведения мока
        when(bookingService.update(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingDto);

        Long userId = 1L;
        Long bookingId = 1L;
        boolean approved = true;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.itemId").value(bookingDto.getItemId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));
    }

    @Test
    void testGetBookings_Success() throws Exception {
        // Подготовка тестовых данных
        BookingDto bookingDto1 = new BookingDto();
        bookingDto1.setId(1L);
        bookingDto1.setItemId(2L);
        bookingDto1.setStatus(BookingStatus.WAITING);

        BookingDto bookingDto2 = new BookingDto();
        bookingDto2.setId(2L);
        bookingDto2.setItemId(3L);
        bookingDto2.setStatus(BookingStatus.APPROVED);

        List<BookingDto> bookingList = Arrays.asList(bookingDto1, bookingDto2);

        // Настройка поведения мока
        when(bookingService.getBookings(anyLong(), anyString())).thenReturn(bookingList);

        Long userId = 1L;

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(bookingList.size()))
                .andExpect(jsonPath("$[0].id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$[0].itemId").value(bookingDto1.getItemId()))
                .andExpect(jsonPath("$[0].status").value(bookingDto1.getStatus().toString()))
                .andExpect(jsonPath("$[1].id").value(bookingDto2.getId()))
                .andExpect(jsonPath("$[1].itemId").value(bookingDto2.getItemId()))
                .andExpect(jsonPath("$[1].status").value(bookingDto2.getStatus().toString()));
    }

    @Test
    void testGetBookings_EmptyList() throws Exception {
        // Настройка поведения мока, чтобы метод возвращал пустой список
        when(bookingService.getBookings(anyLong(), anyString())).thenReturn(List.of());

        Long userId = 1L;

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void testGetOwnerBookings_Success() throws Exception {
        // Подготовка тестовых данных
        BookingDto bookingDto1 = new BookingDto();
        bookingDto1.setId(1L);
        bookingDto1.setItemId(2L);
        bookingDto1.setStatus(BookingStatus.WAITING);

        BookingDto bookingDto2 = new BookingDto();
        bookingDto2.setId(2L);
        bookingDto2.setItemId(3L);
        bookingDto2.setStatus(BookingStatus.APPROVED);

        List<BookingDto> bookingList = Arrays.asList(bookingDto1, bookingDto2);

        // Настройка поведения мока
        when(bookingService.getOwnerBookings(anyLong(), anyString())).thenReturn(bookingList);

        Long ownerId = 1L;

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(bookingList.size()))
                .andExpect(jsonPath("$[0].id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$[0].itemId").value(bookingDto1.getItemId()))
                .andExpect(jsonPath("$[0].status").value(bookingDto1.getStatus().toString()))
                .andExpect(jsonPath("$[1].id").value(bookingDto2.getId()))
                .andExpect(jsonPath("$[1].itemId").value(bookingDto2.getItemId()))
                .andExpect(jsonPath("$[1].status").value(bookingDto2.getStatus().toString()));
    }

    @Test
    void testGetOwnerBookings_EmptyList() throws Exception {
        // Настройка мока, чтобы метод возвращал пустой список
        when(bookingService.getOwnerBookings(anyLong(), anyString())).thenReturn(List.of());

        Long ownerId = 1L;

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

}