package ru.list.surkovr.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUsers extends BaseResponse {

    private List<UserSimpleDto> users;

    public ResponseUsers(boolean success) {
        super(success);
    }

    public ResponseUsers(boolean success, List<UserSimpleDto> users) {
        super(success);
        this.users = users;
    }
}
