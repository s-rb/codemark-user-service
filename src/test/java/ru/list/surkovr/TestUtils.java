package ru.list.surkovr;

import ru.list.surkovr.model.entities.Role;
import ru.list.surkovr.model.entities.User;

import java.util.List;

public class TestUtils {

    public static User createUserVasyaWithRoles() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Vasya");
        user.setLogin("vasya_login");
        user.setPassword("Password7");
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        role.setId(1);
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");
        roleUser.setId(2);
        user.setRoles(List.of(role, roleUser));
        return user;
    }

    public static User createUserPetyaWithRoles() {
        Long userId = 2L;
        User user = new User();
        user.setId(userId);
        user.setName("Petya");
        user.setLogin("petya_login");
        user.setPassword("Password7");
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        role.setId(1);
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");
        roleUser.setId(2);
        user.setRoles(List.of(role, roleUser));
        return user;
    }
}
