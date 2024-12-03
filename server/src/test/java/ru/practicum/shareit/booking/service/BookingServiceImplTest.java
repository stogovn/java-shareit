package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class BookingServiceImplTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void testCreateBooking_Success() {
        // создаём пользователей
        User booker = new User(null, "John Doe", "john.doe@example.com");
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(booker);
        userRepository.save(owner);

        // Создаем вещь и сохраняем её в базе данных
        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        // Создаем DTO для бронирования
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        // создаем бронирование
        BookingDto createdBookingDto = bookingService.create(booker.getId(), bookingDto);

        // проверяем, что бронирование было создано корректно
        assertThat(createdBookingDto).isNotNull();
        assertThat(createdBookingDto.getItemId()).isEqualTo(item.getId());
        assertThat(createdBookingDto.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void testCreateBooking_ItemNotFound() {
        // Arrange: создаём пользователя
        User booker = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(booker);

        // Act & Assert: ожидаем исключение EntityNotFoundException
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(999L); // Несуществующий itemId
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(EntityNotFoundException.class, () -> bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void testCreateBooking_UserCannotBookOwnItem() {
        // создаём пользователя и вещь
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        // ожидаем исключение ValidationException
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.create(owner.getId(), bookingDto));
    }

    @Test
    void testCreateBooking_ItemNotAvailable() {
        // создаём пользователя и вещь
        User booker = new User(null, "John Doe", "john.doe@example.com");
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(booker);
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(false); // Сделаем вещь недоступной
        itemRepository.save(item);

        // ожидаем исключение ValidationException
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void testCreateBooking_ItemAlreadyBooked() {
        // создаём пользователя и вещь
        User booker = new User(null, "John Doe", "john.doe@example.com");
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(booker);
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        // Создаем существующее бронирование для проверки
        Booking existingBooking = new Booking();
        existingBooking.setItem(item);
        existingBooking.setBooker(booker);
        existingBooking.setStatus(BookingStatus.WAITING);
        existingBooking.setStart(LocalDateTime.now().plusDays(1));
        existingBooking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(existingBooking);

        // ожидаем исключение ValidationException при попытке создания пересекающегося бронирования
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(4));

        assertThrows(ValidationException.class, () -> bookingService.create(owner.getId(), bookingDto));
    }

    @Test
    void testGetBookingById_Success() {
        // создаем пользователей и вещь
        User booker = new User(null, "John Doe", "john.doe@example.com");
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(booker);
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(booking);

        // вызываем метод и проверяем результат
        BookingDto bookingDto = bookingService.getBookingById(booker.getId(), booking.getId());

        // проверяем, что полученное бронирование соответствует ожидаемому
        assertNotNull(bookingDto);
        assertEquals(booking.getId(), bookingDto.getId());
        assertEquals(booking.getItem().getId(), bookingDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), bookingDto.getBooker().getId());
        assertEquals(booking.getStatus(), bookingDto.getStatus());
    }

    @Test
    void testGetBookingById_NotFound() {
        // создаем пользователя
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        // проверяем, что метод выбрасывает исключение при отсутствии бронирования
        assertThrows(EntityNotFoundException.class, () -> bookingService.getBookingById(user.getId(), 999L));
    }

    @Test
    void testUpdateBookingStatus_Success() {
        // создаем пользователей, владельца и бронирующего
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        User booker = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(owner);
        userRepository.save(booker);

        // Создаем вещь владельца
        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        // Создаем бронирование
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(booking);

        // обновляем статус бронирования владельцем
        BookingDto updatedBookingDto = bookingService.update(owner.getId(), booking.getId(), true);

        // проверяем, что статус обновился
        assertNotNull(updatedBookingDto);
        assertEquals(BookingStatus.APPROVED, updatedBookingDto.getStatus());
    }

    @Test
    void testUpdateBookingStatus_NotOwner() {
        // создаем пользователей
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        User notOwner = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(owner);
        userRepository.save(notOwner);

        // Создаем вещь владельца
        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        // Создаем бронирование
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(notOwner);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(booking);

        // проверяем, что выбрасывается исключение ValidationException
        assertThrows(ValidationException.class, () -> bookingService.update(notOwner.getId(), booking.getId(), true));
    }

    @Test
    void testUpdateBookingStatus_NotFound() {
        // создаем пользователя
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        // проверяем, что выбрасывается исключение при попытке обновить несуществующее бронирование
        assertThrows(EntityNotFoundException.class, () -> bookingService.update(owner.getId(), 999L, true));
    }

    @Test
    void testGetBookings_Current() {
        // создаем пользователей и вещи
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking currentBooking = new Booking();
        currentBooking.setItem(item);
        currentBooking.setBooker(user);
        currentBooking.setStatus(BookingStatus.APPROVED);
        currentBooking.setStart(LocalDateTime.now().minusHours(1));
        currentBooking.setEnd(LocalDateTime.now().plusHours(1));
        bookingRepository.save(currentBooking);

        // получаем текущие бронирования
        List<BookingDto> bookings = bookingService.getBookings(user.getId(), "CURRENT");

        // Assert: проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(currentBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetBookings_Past() {
        // создаем пользователя и его бронирование
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setBooker(user);
        pastBooking.setStatus(BookingStatus.APPROVED);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(pastBooking);

        // получаем прошедшие бронирования
        List<BookingDto> bookings = bookingService.getBookings(user.getId(), "PAST");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(pastBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetBookings_Future() {
        // создаем пользователя и его будущее бронирование
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking futureBooking = new Booking();
        futureBooking.setItem(item);
        futureBooking.setBooker(user);
        futureBooking.setStatus(BookingStatus.APPROVED);
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        bookingRepository.save(futureBooking);

        // получаем будущие бронирования
        List<BookingDto> bookings = bookingService.getBookings(user.getId(), "FUTURE");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(futureBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetBookings_Waiting() {
        // создаем пользователя и его бронирование со статусом WAITING
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking waitingBooking = new Booking();
        waitingBooking.setItem(item);
        waitingBooking.setBooker(user);
        waitingBooking.setStatus(BookingStatus.WAITING);
        waitingBooking.setStart(LocalDateTime.now().plusDays(1));
        waitingBooking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(waitingBooking);

        // получаем бронирования со статусом WAITING
        List<BookingDto> bookings = bookingService.getBookings(user.getId(), "WAITING");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(waitingBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetBookings_Rejected() {
        // создаем пользователя и его бронирование со статусом REJECTED
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking rejectedBooking = new Booking();
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(user);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        rejectedBooking.setStart(LocalDateTime.now().plusDays(1));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(rejectedBooking);

        // получаем бронирования со статусом REJECTED
        List<BookingDto> bookings = bookingService.getBookings(user.getId(), "REJECTED");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(rejectedBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetBookings_UnknownState() {
        // создаем пользователя и его бронирование
        User user = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(user);

        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(booking);

        // получаем бронирования с неизвестным состоянием
        List<BookingDto> bookings = bookingService.getBookings(user.getId(), "UNKNOWN");

        // проверяем, что возвращаются все бронирования
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetOwnerBookings_Current() {
        // создаем владельца и вещь
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        User booker = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(booker);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking currentBooking = new Booking();
        currentBooking.setItem(item);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);
        currentBooking.setStart(LocalDateTime.now().minusHours(1));
        currentBooking.setEnd(LocalDateTime.now().plusHours(1));
        bookingRepository.save(currentBooking);

        // получаем текущие бронирования для владельца
        List<BookingDto> bookings = bookingService.getOwnerBookings(owner.getId(), "CURRENT");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(currentBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetOwnerBookings_Past() {
        // создаем владельца и его бронирование
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        User booker = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(booker);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(pastBooking);

        // получаем прошедшие бронирования для владельца
        List<BookingDto> bookings = bookingService.getOwnerBookings(owner.getId(), "PAST");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(pastBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetOwnerBookings_Future() {
        // создаем владельца и его будущее бронирование
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        User booker = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(booker);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking futureBooking = new Booking();
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.APPROVED);
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        bookingRepository.save(futureBooking);

        // получаем будущие бронирования для владельца
        List<BookingDto> bookings = bookingService.getOwnerBookings(owner.getId(), "FUTURE");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(futureBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetOwnerBookings_Waiting() {
        // создаем владельца и его бронирование со статусом WAITING
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        User booker = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(booker);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking waitingBooking = new Booking();
        waitingBooking.setItem(item);
        waitingBooking.setBooker(booker);
        waitingBooking.setStatus(BookingStatus.WAITING);
        waitingBooking.setStart(LocalDateTime.now().plusDays(1));
        waitingBooking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(waitingBooking);

        // получаем бронирования со статусом WAITING для владельца
        List<BookingDto> bookings = bookingService.getOwnerBookings(owner.getId(), "WAITING");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(waitingBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetOwnerBookings_Rejected() {
        // создаем владельца и его бронирование со статусом REJECTED
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        User booker = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(booker);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking rejectedBooking = new Booking();
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(booker);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        rejectedBooking.setStart(LocalDateTime.now().plusDays(1));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(rejectedBooking);

        // получаем бронирования со статусом REJECTED для владельца
        List<BookingDto> bookings = bookingService.getOwnerBookings(owner.getId(), "REJECTED");

        // проверяем, что возвращается одно бронирование
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(rejectedBooking.getId(), bookings.getFirst().getId());
    }

    @Test
    void testGetOwnerBookings_UnknownState() {
        // создаем владельца и его бронирование
        User owner = new User(null, "Jane Smith", "jane.smith@example.com");
        userRepository.save(owner);

        User booker = new User(null, "John Doe", "john.doe@example.com");
        userRepository.save(booker);

        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        bookingRepository.save(booking);

        // получаем бронирования с неизвестным состоянием для владельца
        List<BookingDto> bookings = bookingService.getOwnerBookings(owner.getId(), "UNKNOWN");

        // проверяем, что возвращаются все бронирования
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.getFirst().getId());
    }
}