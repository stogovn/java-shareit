package ru.practicum.shareit.item.mapper;


import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import static org.junit.jupiter.api.Assertions.assertNull;

class CommentMapperTest {

    private final CommentMapper commentMapper = new CommentMapperImpl();

    @Test
    void testToCommentDto_NullComment() {
        // Передаем null в метод
        CommentDto result = commentMapper.toCommentDto(null);

        // Проверяем, что результат null
        assertNull(result, "Метод toCommentDto должен вернуть null, если входной параметр comment равен null");
    }

    @Test
    void testToComment_NullDtoItemAuthor() {
        // Передаем null в метод
        Comment result = commentMapper.toComment(null, null, null);

        // Проверяем, что результат null
        assertNull(result, "Метод toComment должен вернуть null, если входные параметры dto, item и author равны null");
    }
}