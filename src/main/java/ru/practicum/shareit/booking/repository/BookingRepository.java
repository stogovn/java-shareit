package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Получение всех бронирований для пользователя, отсортированных по дате
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId ORDER BY b.start DESC")
    List<Booking> findAllByUserIdOrderByStartDesc(@Param("userId") Long userId);

    // Текущие бронирования
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND :now BETWEEN b.start AND b.end ORDER BY b.start DESC")
    List<Booking> findCurrentBookings(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Завершённые бронирования
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastBookings(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Будущие бронирования
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureBookings(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Ожидающие подтверждения
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findWaitingBookings(@Param("userId") Long userId);

    // Отклонённые бронирования
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.status = 'REJECTED' ORDER BY b.start DESC")
    List<Booking> findRejectedBookings(@Param("userId") Long userId);

    // Все бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId);

    // Текущие бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND :now BETWEEN b.start AND b.end ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Завершённые бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastBookingsByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Будущие бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureBookingsByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Ожидающие подтверждения бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findWaitingBookingsByOwner(@Param("ownerId") Long ownerId);

    // Отклонённые бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'REJECTED' ORDER BY b.start DESC")
    List<Booking> findRejectedBookingsByOwner(@Param("ownerId") Long ownerId);

    // Метод для поиска всех бронирований для определенной вещи
    List<Booking> findByItemId(Long itemId);

    // Метод для поиска ближайшего предстоящего бронирования
    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);

    // Метод для поиска последнего завершенного бронирования
    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime now);

    // Метод для проверки завершал ли пользователь бронирование вещи
    boolean existsByItemIdAndBookerIdAndEndBefore(Long itemId, Long bookerId, LocalDateTime endDate);

}
