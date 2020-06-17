package ru.list.surkovr.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.list.surkovr.model.entities.Role;
import ru.list.surkovr.model.entities.User;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRolesDto extends UserSimpleDto {

    private List<String> roles;

    public UserRolesDto(String name, String login, String password) {
        super(name, login, password);
    }

    public UserRolesDto(String name, String login, String password, List<String> roles) {
        super(name, login, password);
        this.roles = roles;
    }

    public static UserRolesDto fromUser(User user) {
        UserRolesDto userRolesDto = new UserRolesDto();
        userRolesDto.setName(user.getName());
        userRolesDto.setLogin(user.getLogin());
        userRolesDto.setPassword(user.getPassword());
        userRolesDto.setRoles(user.getRoles().stream()
                .map(Role::getName).collect(Collectors.toList()));
        return userRolesDto;
    }
}
