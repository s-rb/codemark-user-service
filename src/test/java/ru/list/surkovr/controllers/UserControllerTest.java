package ru.list.surkovr.controllers;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.list.surkovr.dto.requests.UserRequestDto;
import ru.list.surkovr.dto.responses.*;
import ru.list.surkovr.model.entities.Role;
import ru.list.surkovr.model.entities.User;
import ru.list.surkovr.services.interfaces.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.list.surkovr.TestUtils.createUserVasyaWithRoles;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testGetAllUsers_success() throws Exception {
        var success = true;
        var vasya = new UserSimpleDto("Vasya", "vasya_login", "Password1");
        var petya = new UserSimpleDto("Petya", "super_petr", "Password2");
        var resultDto = new ResponseUsers(success);
        resultDto.setUsers(List.of(vasya, petya));
        given(userService.findAll()).willReturn(ResponseEntity.ok(resultDto));
        mockMvc.perform(get("/api/users/list").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", Is.is(success)))
                .andExpect(jsonPath("$.users", hasSize(2)))
                .andExpect(jsonPath("$.users[0].name", Is.is(vasya.getName())))
                .andExpect(jsonPath("$.users[0].login", Is.is(vasya.getLogin())))
                .andExpect(jsonPath("$.users[0].password", Is.is(vasya.getPassword())))
                .andExpect(jsonPath("$.users[1].name", Is.is(petya.getName())))
                .andExpect(jsonPath("$.users[1].login", Is.is(petya.getLogin())))
                .andExpect(jsonPath("$.users[1].password", Is.is(petya.getPassword())));
    }

    @Test
    public void testGetUser_success() throws Exception {
        Long userId = 1L;
        User user = createUserVasyaWithRoles();

        boolean success = true;
        var resultDto = new ResponseUserWithRoles(success);
        resultDto.setUser(UserRolesDto.fromUser(user));

        given(userService.getUserById(userId)).willReturn(ResponseEntity.ok(resultDto));
        mockMvc.perform(get("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", Is.is(success)))
                .andExpect(jsonPath("$.user.name", Is.is(user.getName())))
                .andExpect(jsonPath("$.user.login", Is.is(user.getLogin())))
                .andExpect(jsonPath("$.user.password", Is.is(user.getPassword())))
                .andExpect(jsonPath("$.user.roles", hasSize(2)))
                .andExpect(jsonPath("$.user.roles[0]", Is.is(user.getRoles().get(0).getName())))
                .andExpect(jsonPath("$.user.roles[1]", Is.is(user.getRoles().get(1).getName())));
    }

    @Test
    public void testDeleteUser_success() throws Exception {
        User user = createUserVasyaWithRoles();
        Long id = user.getId();
        boolean success = true;
        given(userService.deleteUser(id)).willReturn(
                ResponseEntity.ok(new BaseResponse(success)));
        mockMvc.perform(delete("/api/users/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", Is.is(success)));
    }

    @Test
    public void testAddUser_success() throws Exception {
        User user = createUserVasyaWithRoles();
        UserRequestDto requestDto = new UserRequestDto(user.getName(),
                user.getLogin(), user.getPassword(),
                user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        given(userService.addUser(requestDto)).willReturn(
                ResponseEntity.ok(new BaseResponse(true)));
        String requestString = "{\"name\": \"Vasya\", \"login\": \"vasya_login\"," +
                "\"password\": \"Password7\", \"roles\": [1, 2]}";
        mockMvc.perform(post("/api/users/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", Is.is(true)));
    }

    @Test
    public void testAddUser_nullLoginNamePassword() throws Exception {
        String requestString = "{\"name\": \"\", \"login\": \"\"," +
                "\"password\": \"\", \"roles\": [1, 2]}";
        String[] errors = {"Отсутствует логин", "Отсутствует имя", "Отсутствует пароль"};

        mockMvc.perform(post("/api/users/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", Is.is(false)))
                .andExpect(jsonPath("$.errors", hasSize(3)))
                .andExpect(jsonPath("$.errors[0]", Is.is(errors[0])))
                .andExpect(jsonPath("$.errors[1]", Is.is(errors[1])))
                .andExpect(jsonPath("$.errors[2]", Is.is(errors[2])));
    }

    @Test
    public void testEditUser_nullLoginNamePassword() throws Exception {
        String requestString = "{\"name\": \"\", \"login\": \"\"," +
                "\"password\": \"\", \"roles\": [1, 2]}";
        String[] errors = {"Отсутствует логин", "Отсутствует имя", "Отсутствует пароль"};

        mockMvc.perform(put("/api/users/edit/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", Is.is(false)))
                .andExpect(jsonPath("$.errors", hasSize(3)))
                .andExpect(jsonPath("$.errors[0]", Is.is(errors[0])))
                .andExpect(jsonPath("$.errors[1]", Is.is(errors[1])))
                .andExpect(jsonPath("$.errors[2]", Is.is(errors[2])));
    }

    @Test
    public void testEditUser_success() throws Exception {
        User user = createUserVasyaWithRoles();
        UserRequestDto requestDto = new UserRequestDto(user.getName() + "a",
                user.getLogin() + "a", user.getPassword() + "a",
                List.of(1));
        Long id = user.getId();

        given(userService.editUser(requestDto, id)).willReturn(
                ResponseEntity.ok(new BaseResponse(true)));
        String requestString = "{\"name\": \"Vasyaa\", \"login\": \"vasya_logina\"," +
                "\"password\": \"Password7a\", \"roles\": [1]}";
        mockMvc.perform(put("/api/users/edit/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", Is.is(true)));
    }
}
