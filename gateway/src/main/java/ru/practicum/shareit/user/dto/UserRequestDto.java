package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

@Data
@AllArgsConstructor
public class UserRequestDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotBlank(message = "Имя обязательно", groups = OnCreate.class)
    @Size(max = 255)
    private String name;
    @Email(message = "Неправильно введён email")
    @NotBlank(message = "Email обязателен", groups = OnCreate.class)
    private String email;

}
