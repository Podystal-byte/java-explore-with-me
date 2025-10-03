package ru.practicum.category.dto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoriesMapper {

    CategoryDto toDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCategory(NewCategoryDto dto);
}
