package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

@Data
public class ItemRequestDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotBlank(message = "Имя обязательно", groups = OnCreate.class)
    @Size(max = 255)
    private String name;
    @NotBlank(message = "Описание обязательно", groups = OnCreate.class)
    @Size(max = 500)
    private String description;
    @NotNull(message = "Поле available обязательно", groups = OnCreate.class)
    private Boolean available;
    @Positive
    private Long requestId;
}
