package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.validation.OnCreate;

import java.time.LocalDateTime;

@Data
public class ItemRequestDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotBlank(message = "Описание обязательно", groups = OnCreate.class)
    @Size(max = 500)
    private String description;
    private UserRequestDto requestor;
    private LocalDateTime created;
}
