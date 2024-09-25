package ru.practicum.shareit;

import java.util.Map;

public abstract class GenerateId<T> {
    protected long getNextId(Map<Long, T> map) {
        long currentMaxId = map.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
