package ru.list.surkovr.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ResponseErrorsDto extends BaseResponse {

    private List<String> errors;

    public void addErrors(String... errors) {
        List<String> errorsList = Arrays.stream(errors)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (this.errors == null) this.errors = new ArrayList<>();
        this.errors.addAll(errorsList);
    }

    public ResponseErrorsDto(boolean success, List<String> errors) {
        super(success);
        this.errors = errors;
    }

    public ResponseErrorsDto(boolean success) {
        super(success);
    }
}
