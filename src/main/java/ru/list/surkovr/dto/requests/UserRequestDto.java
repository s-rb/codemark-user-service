package ru.list.surkovr.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    private String name;
    private String login;
    private String password;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> roles;

    public UserRequestDto(String name, String login, String password) {
        this.name = name;
        this.login = login;
        this.password = password;
    }
}
