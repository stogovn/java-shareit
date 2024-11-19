package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Получение всех бронирований для пользователя, отсортированных по дате
    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    // Текущие бронирования
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.booker.id = :userId
            AND :now BETWEEN b.start AND b.end
            ORDER BY b.start DESC
            """)
    List<Booking> findCurrentBookings(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Завершённые бронирования
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime now);

    // Будущие бронирования
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    // Все бронирования для вещей владельца
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    // Текущие бронирования для вещей владельца
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.item.owner.id = :ownerId
            AND :now BETWEEN b.start AND b.end
            ORDER BY b.start DESC
            """)
    List<Booking> findCurrentBookingsByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Завершённые бронирования для вещей владельца
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    // Будущие бронирования для вещей владельца
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    // Метод для поиска всех бронирований для определенной вещи
    List<Booking> findByItemId(Long itemId);

    // Метод для поиска ближайшего предстоящего бронирования
    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);

    // Метод для поиска последнего завершенного бронирования
    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime now);

    // Метод для проверки завершал ли пользователь бронирование вещи
    boolean existsByItemIdAndBookerIdAndEndBefore(Long itemId, Long bookerId, LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId AND (b.booker.id = :userId OR b.item.owner.id = :userId)")
    Optional<Booking> findByIdAndBookerIdOrItemOwnerId(@Param("bookingId") Long bookingId,
                                                       @Param("userId") Long userId);


    List<Booking> findByItemIdAndStartBeforeAndEndAfter(Long itemId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemIdIn(List<Long> itemIds);

}
