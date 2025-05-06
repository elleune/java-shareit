package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingInDto {
	@NotNull(message = "Дата начала не может быть пустой")
	@FutureOrPresent(message = "Дата начала должна быть в будущем или настоящем")
	private LocalDateTime start;

	@NotNull(message = "Дата окончания не может быть пустой")
	@Future(message = "Дата окончания должна быть в будущем")
	private LocalDateTime end;

	@NotNull(message = "ID вещи не может быть пустым")
	private Long itemId;
}
