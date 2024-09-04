package ru.practicum.shareit.item.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.GenerateId;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Getter
@Component
public class InMemoryItemStorage extends GenerateId<Item> implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();


    @Override
    public Item create(Long userId, Item item) {
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new ValidationException("Имя не должно быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
            throw new ValidationException("Описание не должно быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Available должен быть указан");
        }
        item.setId(getNextId(items));
        item.setOwner(userId);
        items.put(item.getId(), item);
        log.info("Добавилась новая вещь с id = {} пользователя с id = {}", item.getId(), userId);

        return item;
    }

    @Override
    public Item update(Long userId, Long id, Item item) {
        Item oldItem = items.get(id);
        if (!Objects.equals(oldItem.getOwner(), userId)) {
            throw new ValidationException("Данный пользователь не может редактировать эту вещь");
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        log.info("Обновили вещь с id = {} пользователя с id = {}", item.getId(), userId);

        return oldItem;
    }

    @Override
    public Item getItemById(long id) {
        return items.get(id);
    }

    @Override
    public Collection<Item> getItemsByUserId(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .toList();
    }

    @Override
    public Collection<Item> searchItems(String text) {
        String search = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(search) ||
                                item.getDescription().toLowerCase().contains(search))
                .filter(Item::getAvailable)
                .toList();
    }
}
