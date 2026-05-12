package com.demo.validator;

import com.demo.repository.DepartmentsRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentExistsValidator implements ConstraintValidator<DepartmentExists, Long> {

    private final DepartmentsRepository departmentsRepository;

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        // Let @NotNull handle nulls so error messages stay specific.
        if (value == null) {
            return true;
        }

        return departmentsRepository.findByIdAndDeletedFalse(value).isPresent();
    }
}

