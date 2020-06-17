package ru.list.surkovr.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUserWithRoles extends BaseResponse {

    private UserRolesDto user;

    public ResponseUserWithRoles(boolean success) {
        super(success);
    }

    public ResponseUserWithRoles(boolean success, UserRolesDto user) {
        super(success);
        this.user = user;
    }
}
