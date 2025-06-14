package ru.practicum.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.validation.CreateGroup;


@Data
public class NewCategoryDto {

    @NotBlank(groups = CreateGroup.class)
    @Size(min = 1, max = 50, message = "Длина названия должна быть >= 1 символа и <= 50", groups = CreateGroup.class)
    private String name;
}
