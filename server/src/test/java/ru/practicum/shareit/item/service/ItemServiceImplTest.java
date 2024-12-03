package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;



    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();
        bookingRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    void testCreateItem() {

        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user = userRepository.save(user);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Need a book");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest = itemRequestRepository.save(itemRequest);

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Book");
        itemDto.setDescription("A good book");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequest.getId());


        ItemDto createdItem = itemService.create(user.getId(), itemDto);


        Assertions.assertNotNull(createdItem);
        Assertions.assertNotNull(createdItem.getId());
        assertEquals(itemDto.getName(), createdItem.getName());
        assertEquals(itemDto.getDescription(), createdItem.getDescription());
        assertEquals(itemDto.getRequestId(), createdItem.getRequestId());


        Item savedItem = itemRepository.findById(createdItem.getId())
                .orElseThrow(() -> new AssertionError("Item not found in the database"));
        assertEquals(user.getId(), savedItem.getOwner().getId());
        assertEquals(itemRequest.getId(), savedItem.getRequest().getId());
    }

    @Test
    void testUpdateItem_Success() {

        User owner = new User();
        owner.setName("Test User");
        owner.setEmail("testuser@example.com");
        User savedUser = userRepository.save(owner);

        Item item = new Item();
        item.setName("Old Name");
        item.setDescription("Old Description");
        item.setAvailable(true);
        item.setOwner(savedUser);
        Item savedItem = itemRepository.save(item);

        ItemDto updateRequest = new ItemDto();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated Description");
        updateRequest.setAvailable(false);


        ItemDto updatedItemDto = itemService.update(savedUser.getId(), savedItem.getId(), updateRequest);


        assertEquals("Updated Name", updatedItemDto.getName());
        assertEquals("Updated Description", updatedItemDto.getDescription());
        assertFalse(updatedItemDto.getAvailable());
    }

    @Test
    void testUpdateItem_UnauthorizedUser_ThrowsException() {

        User owner = new User();
        owner.setName("Owner User");
        owner.setEmail("owner@example.com");
        User savedOwner = userRepository.save(owner);

        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        User savedAnotherUser = userRepository.save(anotherUser);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setAvailable(true);
        item.setOwner(savedOwner);
        Item savedItem = itemRepository.save(item);

        ItemDto updateRequest = new ItemDto();
        updateRequest.setName("Unauthorized Update");


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.update(savedAnotherUser.getId(), savedItem.getId(), updateRequest)
        );

        assertEquals("Данный пользователь не может редактировать эту вещь", exception.getMessage());
    }

    @Test
    void testUpdateItem_ItemNotFound_ThrowsException() {

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        User savedUser = userRepository.save(user);

        ItemDto updateRequest = new ItemDto();
        updateRequest.setName("Non-existent Item");


        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemService.update(savedUser.getId(), 999L, updateRequest)
        );

        assertEquals("Item not found with id: 999", exception.getMessage());
    }

    @Test
    void testGetAllItemsForUser() {
        // Создаем тестового пользователя
        User owner = new User();
        owner.setName("Test User");
        owner.setEmail("testuser@example.com");
        userRepository.save(owner);

        // Создаем тестовые вещи
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Description for Item 1");
        item1.setAvailable(true);
        item1.setOwner(owner);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setDescription("Description for Item 2");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        // Создаем тестовые бронирования для вещей
        Booking booking1 = new Booking();
        booking1.setItem(item1);
        booking1.setStatus(BookingStatus.WAITING);
        booking1.setStart(java.time.LocalDateTime.now().minusDays(1));
        booking1.setEnd(java.time.LocalDateTime.now().plusDays(1));
        bookingRepository.save(booking1);

        Booking booking2 = new Booking();
        booking2.setItem(item2);
        booking2.setStatus(BookingStatus.WAITING);
        booking2.setStart(java.time.LocalDateTime.now().plusDays(1));
        booking2.setEnd(java.time.LocalDateTime.now().plusDays(2));
        bookingRepository.save(booking2);

        Collection<ItemBookingsDto> result = itemService.getAllItemsForUser(owner.getId());

        assertThat(result)
                .isNotNull()
                .hasSize(2);

        // Проверка наличия бронирований и их деталей
        assertThat(result.stream().map(ItemBookingsDto::getName))
                .containsExactlyInAnyOrder("Item 1", "Item 2");

        // Проверка, что бронирования для каждого элемента корректны
        result.forEach(itemDto -> {
            if ("Item 1".equals(itemDto.getName())) {
                assertThat(itemDto.getBookings()).hasSize(1);
                assertThat(itemDto.getBookings().getFirst().getStart()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
                assertThat(itemDto.getBookings().getFirst().getEnd()).isAfterOrEqualTo(java.time.LocalDateTime.now());
            } else if ("Item 2".equals(itemDto.getName())) {
                assertThat(itemDto.getBookings()).hasSize(1);
                assertThat(itemDto.getBookings().getFirst().getStart()).isAfter(java.time.LocalDateTime.now());
                assertThat(itemDto.getBookings().getFirst().getEnd()).isAfter(java.time.LocalDateTime.now());
            }
        });
    }

    @Test
    void testGetItemById_WithBookingsAndComments() {
        // Создаем тестового пользователя
        User owner = new User();
        owner.setName("Test User");
        owner.setEmail("testuser@example.com");
        userRepository.save(owner);

        // Создаем тестовый предмет
        Item item = new Item();
        item.setName("Item 1");
        item.setDescription("Description for Item 1");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        // Создаем комментарии для предмета
        Comment comment1 = new Comment();
        comment1.setItem(item);
        comment1.setText("Great item!");
        comment1.setAuthor(owner);
        comment1.setCreated(LocalDateTime.now());
        commentRepository.save(comment1);

        // Создаем бронирования для предмета
        Booking nextBooking = new Booking();
        nextBooking.setItem(item);
        nextBooking.setStatus(BookingStatus.WAITING);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));
        nextBooking.setBooker(owner);
        bookingRepository.save(nextBooking);

        Booking lastBooking = new Booking();
        lastBooking.setItem(item);
        lastBooking.setStatus(BookingStatus.WAITING);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));
        lastBooking.setBooker(owner);
        bookingRepository.save(lastBooking);

        ItemInfoDto result = itemService.getItemById(owner.getId(), item.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Item 1");
        assertThat(result.getDescription()).isEqualTo("Description for Item 1");
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().getFirst().getText()).isEqualTo("Great item!");

        // Проверка бронирований для владельца
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getNextBooking().getStart()).isAfter(LocalDateTime.now());
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getLastBooking().getEnd()).isBefore(LocalDateTime.now());
    }

    @Test
    void testGetItemById_WithNoBookingsOrComments() {
        // Создаем тестового пользователя
        User owner = new User();
        owner.setName("Test User");
        owner.setEmail("testuser@example.com");
        userRepository.save(owner);

        // Создаем тестовый предмет
        Item item = new Item();
        item.setName("Item 2");
        item.setDescription("Description for Item 2");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        ItemInfoDto result = itemService.getItemById(owner.getId(), item.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Item 2");
        assertThat(result.getDescription()).isEqualTo("Description for Item 2");
        assertThat(result.getComments()).isEmpty();
        assertThat(result.getNextBooking()).isNull();
        assertThat(result.getLastBooking()).isNull();
    }

    @Test
    void testGetItemById_ItemNotFound() {
        // Создаем тестового пользователя
        User user = new User();
        user.setName("Test User");
        user.setEmail("testuser@example.com");
        userRepository.save(user);


        assertThatThrownBy(() -> itemService.getItemById(user.getId(), 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Item not found");
    }

    @Test
    void testSearchItems_ReturnsMatchingItems() {
        // Создаем пользователя
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        userRepository.save(user);

        // Создаем доступную вещь, соответствующую запросу
        Item item1 = new Item();
        item1.setName("Laptop");
        item1.setDescription("Gaming Laptop");
        item1.setAvailable(true);
        item1.setOwner(user);
        itemRepository.save(item1);

        // Создаем недоступную вещь, соответствующую запросу
        Item item2 = new Item();
        item2.setName("Old Laptop");
        item2.setDescription("Broken Laptop");
        item2.setAvailable(false);
        item2.setOwner(user);
        itemRepository.save(item2);

        // Создаем вещь, не соответствующую запросу
        Item item3 = new Item();
        item3.setName("Tablet");
        item3.setDescription("Android Tablet");
        item3.setAvailable(true);
        item3.setOwner(user);
        itemRepository.save(item3);

        Collection<ItemDto> result = itemService.searchItems("Laptop");

        assertThat(result).hasSize(1);
        assertThat(result).extracting(ItemDto::getName).containsExactly("Laptop");
    }

    @Test
    void testSearchItems_NoMatches() {
        // Создаем пользователя
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        userRepository.save(user);

        // Создаем несколько вещей
        Item item1 = new Item();
        item1.setName("Chair");
        item1.setDescription("Wooden Chair");
        item1.setAvailable(true);
        item1.setOwner(user);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Table");
        item2.setDescription("Glass Table");
        item2.setAvailable(true);
        item2.setOwner(user);
        itemRepository.save(item2);

        Collection<ItemDto> result = itemService.searchItems("Laptop");

        assertThat(result).isEmpty();
    }

    @Test
    void testSearchItems_EmptyText_ReturnsEmptyList() {
        // Создаем пользователя
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        userRepository.save(user);

        // Создаем несколько вещей
        Item item1 = new Item();
        item1.setName("Chair");
        item1.setDescription("Wooden Chair");
        item1.setAvailable(true);
        item1.setOwner(user);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Table");
        item2.setDescription("Glass Table");
        item2.setAvailable(true);
        item2.setOwner(user);
        itemRepository.save(item2);

        Collection<ItemDto> result = itemService.searchItems("");

        assertThat(result).isEmpty();
    }

    @Test
    void testAddComment_Success() {
        // Создаем пользователя, вещь и бронирование
        User user = userRepository.save(new User(null, "User", "user@example.com"));
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));

        Item item = itemRepository.save(new Item(null, "Item", "Description", true, owner, null));

        LocalDateTime now = LocalDateTime.now();
        bookingRepository.save(new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED));

        CommentDto commentDto = new CommentDto(null, "Great item!", user.getName(), null);

        // Добавляем комментарий
        CommentDto savedComment = itemService.addComment(item.getId(), user.getId(), commentDto);

        // Проверяем, что комментарий был сохранен
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getText()).isEqualTo("Great item!");
        assertThat(savedComment.getAuthorName()).isEqualTo(user.getName());

        // Дополнительно: Проверяем, что комментарий существует в репозитории
        List<Comment> comments = commentRepository.findByItemId(item.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().getText()).isEqualTo("Great item!");
    }

    @Test
    void testAddComment_ItemNotFound() {
        // Создаем пользователя
        User user = userRepository.save(new User(null, "User", "user@example.com"));

        CommentDto commentDto = new CommentDto(null, "Great item!", user.getName(), null);

        // Ожидаем исключение EntityNotFoundException
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemService.addComment(999L, user.getId(), commentDto)
        );
        assertThat(exception.getMessage()).isEqualTo("Вещь не найдена");
    }

    @Test
    void testAddComment_UserNotFound() {
        // Создаем вещь
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description", true, owner, null));

        CommentDto commentDto = new CommentDto(null, "Great item!", "NonExistentUser", null);

        // Ожидаем исключение EntityNotFoundException
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemService.addComment(item.getId(), 999L, commentDto)
        );
        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");
    }

    @Test
    void testAddComment_UserHasNoBookings() {
        // Создаем пользователя и вещь без бронирования
        User user = userRepository.save(new User(null, "User", "user@example.com"));
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));

        Item item = itemRepository.save(new Item(null, "Item", "Description", true, owner, null));

        CommentDto commentDto = new CommentDto(null, "Great item!", user.getName(), null);

        // Ожидаем исключение ValidationException
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.addComment(item.getId(), user.getId(), commentDto)
        );
        assertThat(exception.getMessage()).isEqualTo("Пользователь не может оставить комментарий, не бронировав товар");
    }
}