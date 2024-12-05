package ru.practicum.shareit.request.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ItemRequestServiceImplTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }

    @Test
    void testCreateItemRequest_Success() {
        // создаём пользователя и сохраняем его в базе данных
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);
        LocalDateTime now = LocalDateTime.now();
        UserDto userDto = userMapper.toUserDto(user);
        // Создаём DTO для запроса
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "Item request description",userDto,now);

        // вызываем метод create и сохраняем результат
        ItemRequestDto createdItemRequestDto = itemRequestService.create(user.getId(), itemRequestDto);

        // проверяем, что запрос был сохранён в базе данных
        assertThat(createdItemRequestDto).isNotNull();
        assertThat(createdItemRequestDto.getDescription()).isEqualTo("Item request description");
        assertThat(createdItemRequestDto.getRequestor().getId()).isEqualTo(user.getId());

        // Проверяем, что объект сохранён в репозитории
        ItemRequest savedItemRequest = itemRequestRepository.findById(createdItemRequestDto.getId()).orElse(null);
        assertThat(savedItemRequest).isNotNull();
        assertThat(savedItemRequest.getDescription()).isEqualTo("Item request description");
    }

    @Test

    void testCreateItemRequest_UserNotFound() {
        UserDto userDto = new UserDto(999L, "test", "test@mail.ru");
        LocalDateTime now = LocalDateTime.now();
        // создаём DTO для запроса
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "Item request description", userDto, now);

        // ожидаем выброс исключения EntityNotFoundException
        assertThrows(EntityNotFoundException.class, () -> {
            itemRequestService.create(999L, itemRequestDto); // Используем несуществующий ID пользователя
        });
    }

    @Test
    void testGetUserRequests_Success() {
        // создаём пользователя и сохраняем его в базе данных
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        // Создаём запрос и сохраняем его в базе данных
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequestor(user);
        itemRequest.setDescription("Item request description");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequestRepository.save(itemRequest);

        // Создаём вещь, связанную с запросом
        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(user);
        item.setAvailable(true);
        item.setRequest(itemRequest);
        itemRepository.save(item);

        // Act: вызываем метод getUserRequests и получаем результат
        List<ItemsRequestDto> userRequests = itemRequestService.getUserRequests(user.getId());

        // Assert: проверяем, что запрос пользователя возвращается корректно
        assertThat(userRequests).isNotEmpty();
        assertThat(userRequests.getFirst().getId()).isEqualTo(itemRequest.getId());
        assertThat(userRequests.getFirst().getDescription()).isEqualTo("Item request description");
        assertThat(userRequests.getFirst().getItems()).isNotEmpty();
        assertThat(userRequests.getFirst().getItems().getFirst().getName()).isEqualTo("Item Name");
    }

    @Test
    void testGetUserRequests_NoRequests() {
        // создаём пользователя без запросов
        User user = new User(null, "Jane Doe", "jane.doe@example.com");
        userRepository.save(user);

        // вызываем метод getUserRequests и получаем результат
        List<ItemsRequestDto> userRequests = itemRequestService.getUserRequests(user.getId());

        // проверяем, что метод возвращает пустой список
        assertThat(userRequests).isEmpty();
    }

    @Test
    void testGetRequestById_Success() {
        // создаём пользователя и сохраняем его в базе данных
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        // Создаём запрос и сохраняем его в базе данных
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequestor(user);
        itemRequest.setDescription("Item request description");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequestRepository.save(itemRequest);

        // Создаём вещь, связанную с запросом
        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(user);
        item.setAvailable(true);
        item.setRequest(itemRequest);
        itemRepository.save(item);

        // вызываем метод getRequestById и получаем результат
        ItemsRequestDto itemsRequestDto = itemRequestService.getRequestById(itemRequest.getId());

        // проверяем, что запрос возвращается корректно
        assertThat(itemsRequestDto).isNotNull();
        assertThat(itemsRequestDto.getId()).isEqualTo(itemRequest.getId());
        assertThat(itemsRequestDto.getDescription()).isEqualTo("Item request description");
        assertThat(itemsRequestDto.getItems()).isNotEmpty();
        assertThat(itemsRequestDto.getItems().getFirst().getName()).isEqualTo("Item Name");
    }

    @Test
    void testGetRequestById_NotFound() {
        // ожидаем исключение EntityNotFoundException
        assertThrows(EntityNotFoundException.class, () -> itemRequestService.getRequestById(999L));
    }
}