package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoriesMapper;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoriesRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriesService {
    private final CategoriesRepository categoriesRepository;
    private final EventRepository eventRepository;
    private final CategoriesMapper categoriesMapper;

    public List<CategoryDto> getCategories(Integer from, Integer size) {
        int offset = from > 0 ? from / size : 0;
        PageRequest page = PageRequest.of(offset, size);
        List<Category> categoriesList = categoriesRepository.findAll(page).getContent();
        return categoriesList.stream().map(categoriesMapper::toDto).collect(Collectors.toList());
    }

    public CategoryDto getCategoriesId(Long catId) {
        Category category = getCategoriesIfExist(catId);
        return categoriesMapper.toDto(category);
    }

    public CategoryDto createCategories(NewCategoryDto newCategoryDto) {
        if (categoriesRepository.existsCategoriesByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с именем '" + newCategoryDto.getName() + "' уже существует");
        }
        if (newCategoryDto.getName().length() > 50) {
            throw new ValidationException("Слишком длинное имя");
        }
        Category category = categoriesRepository.save(categoriesMapper.toCategory(newCategoryDto));
        return categoriesMapper.toDto(category);
    }

    public void deleteCategories(Long catId) {
        Category category = getCategoriesIfExist(catId);

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Невозможно удалить категорию - существуют связанные события");
        }

        categoriesRepository.deleteById(catId);
    }

    public CategoryDto updateCategories(CategoryDto categoryDto) {
        Category categories = getCategoriesIfExist(categoryDto.getId());

        if (categoriesRepository.existsCategoriesByNameAndIdNot(categoryDto.getName(), categoryDto.getId())) {
            throw new ConflictException("Категория с именем '" + categoryDto.getName() + "' уже существует");
        }

        if (categoryDto.getName().length() > 50) {
            throw new ValidationException("Слишком длинное имя");
        }

        categories.setName(categoryDto.getName());
        Category updatedCategory = categoriesRepository.save(categories);
        return categoriesMapper.toDto(updatedCategory);
    }

    private Category getCategoriesIfExist(Long catId) {
        return categoriesRepository.findById(catId).orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));
    }
}
