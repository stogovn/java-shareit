package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRequestDtoTest {
    private final JacksonTester<CommentRequestDto> json;

    @Test
    void testSerializeCommentRequestDto() throws Exception {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setId(1L);
        commentRequestDto.setText("This is a valid comment.");

        JsonContent<CommentRequestDto> result = json.write(commentRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("This is a valid comment.");
    }

    @Test
    void testCommentNotNull() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText(null);

        assertThat(commentRequestDto.getText()).isNull();
    }

    @Test
    void testCommentBlank() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("");

        assertThat(commentRequestDto.getText()).isEmpty();
    }

    @Test
    void testCommentTooLong() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        String longText = "A".repeat(501); // 501 символ

        commentRequestDto.setText(longText);

        assertThat(commentRequestDto.getText()).hasSizeGreaterThan(500);
    }

    @Test
    void testValidComment() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("This is a valid comment.");

        assertThat(commentRequestDto.getText()).isEqualTo("This is a valid comment.");
    }
}