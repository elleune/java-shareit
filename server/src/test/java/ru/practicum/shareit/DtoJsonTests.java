package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
public class DtoJsonTests {

    @Autowired
    private JacksonTester<BookingInDto> bookingInDtoJson;
    @Autowired
    private JacksonTester<UserDto> userDtoJson;
    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestDtoJson;

    @Test
    public void testBookingInDtoSerialization() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BookingInDto dto = BookingInDto.builder()
                .start(now)
                .end(now.plusDays(1))
                .itemId(1L)
                .build();

        JsonContent<BookingInDto> result = bookingInDtoJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(now.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }

    @Test
    public void testUserDtoValidation() {
        UserDto validDto = UserDto.builder()
                .name("name")
                .email("email@example.com")
                .build();

        UserDto invalidDto = UserDto.builder()
                .name("")
                .email("invalid")
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<UserDto>> validViolations = validator.validate(validDto);
        Set<ConstraintViolation<UserDto>> invalidViolations = validator.validate(invalidDto);

        assertTrue(validViolations.isEmpty());
        assertEquals(2, invalidViolations.size());
    }
}
