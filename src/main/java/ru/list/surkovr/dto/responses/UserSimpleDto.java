package ru.list.surkovr.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.list.surkovr.model.entities.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSimpleDto {

    private String name;
    private String login;
    private String password;

    public static UserSimpleDto fromUser(User user) {
        UserSimpleDto userSimpleDto = new UserSimpleDto();
        userSimpleDto.setLogin(user.getLogin());
        userSimpleDto.setName(user.getName());
        userSimpleDto.setPassword(user.getPassword());
        return userSimpleDto;
    }
}
