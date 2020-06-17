package ru.list.surkovr.services;

import liquibase.integration.spring.SpringLiquibase;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.list.surkovr.dto.requests.UserRequestDto;
import ru.list.surkovr.dto.responses.*;
import ru.list.surkovr.model.entities.Role;
import ru.list.surkovr.model.entities.User;
import ru.list.surkovr.repositories.RoleRepository;
import ru.list.surkovr.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static ru.list.surkovr.TestUtils.createUserPetyaWithRoles;
import static ru.list.surkovr.TestUtils.createUserVasyaWithRoles;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = UserServiceImplTest.UserServiceTestConfiguration.class)
public class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @InjectMocks
    UserServiceImpl userService;

    @MockBean
    SpringLiquibase liquibase;

    @Configuration
    @ComponentScan("ru.list.surkovr")
    public static class UserServiceTestConfiguration {
    }

    @Test
    public void testEditUser_success() {
        User userVasya = createUserVasyaWithRoles();
        User userPetya = createUserPetyaWithRoles();
        UserRequestDto requestDto = new UserRequestDto(userVasya.getName(), userVasya.getLogin(),
                userVasya.getPassword(), userVasya.getRoles().stream()
                .map(Role::getId).collect(Collectors.toList()));
        given(userRepository.findById(userPetya.getId())).willReturn(Optional.of(userPetya));
        given(userRepository.findUserByLogin(requestDto.getLogin())).willReturn(null);
        var expected = ResponseEntity.ok(new BaseResponse(true));
        var actual = userService.editUser(requestDto, userPetya.getId());

        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), is(expected.getStatusCodeValue()));
        assertThat(actual.getBody(), is(expected.getBody()));
    }

    @Test
    public void testEditUser_notFound() {
        User userVasya = createUserVasyaWithRoles();
        Long id = 3L;
        UserRequestDto requestDto = new UserRequestDto(userVasya.getName(), userVasya.getLogin(),
                userVasya.getPassword(), userVasya.getRoles().stream()
                .map(Role::getId).collect(Collectors.toList()));
        given(userRepository.findById(id)).willReturn(Optional.empty());
        var expected = ResponseEntity.badRequest().body(new ResponseErrorsDto(false,
                List.of("Пользователь с таким id " + id + " не существует")));
        var actual = userService.editUser(requestDto, id);

        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), is(expected.getStatusCodeValue()));
        assertThat(actual.getBody(), is(expected.getBody()));
    }

    @Test
    public void testEditUser_wrongPassAndLoginExists() {
        User userVasya = createUserVasyaWithRoles();
        User userPetya = createUserPetyaWithRoles();
        Long id = 1L;
        UserRequestDto requestDto = new UserRequestDto(userVasya.getName(), userPetya.getLogin(),
                "pass", userVasya.getRoles().stream()
                .map(Role::getId).collect(Collectors.toList()));
        given(userRepository.findById(id)).willReturn(Optional.of(userVasya));
        given(userRepository.findUserByLogin(userPetya.getLogin())).willReturn(userPetya);

        var expected = ResponseEntity.badRequest().body(
                new ResponseErrorsDto(false,
                        List.of("Пароль не соответствует требованиям",
                                "Пользователь с таким логином " + userPetya.getLogin()
                                        + " уже существует")));
        var actual = userService.editUser(requestDto, id);

        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), is(expected.getStatusCodeValue()));
        assertThat(actual.getBody(), is(expected.getBody()));
    }

    @Test
    public void testEditUser_LoginExists() {
        User userVasya = createUserVasyaWithRoles();
        User userPetya = createUserPetyaWithRoles();
        Long id = 1L;
        UserRequestDto requestDto = new UserRequestDto(userVasya.getName(), userPetya.getLogin(),
                userVasya.getPassword(), userVasya.getRoles().stream()
                .map(Role::getId).collect(Collectors.toList()));
        given(userRepository.findById(id)).willReturn(Optional.of(userVasya));
        given(userRepository.findUserByLogin(userPetya.getLogin())).willReturn(userPetya);

        var expected = ResponseEntity.badRequest().body(
                new ResponseErrorsDto(false,
                        List.of("Пользователь с таким логином " + userPetya.getLogin()
                                        + " уже существует")));
        var actual = userService.editUser(requestDto, id);

        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), is(expected.getStatusCodeValue()));
        assertThat(actual.getBody(), is(expected.getBody()));
    }

    @Test
    public void testFindAllUsers_success() {
        User userVasya = createUserVasyaWithRoles();
        User userPetya = createUserPetyaWithRoles();
        given(userRepository.findAll()).willReturn(List.of(userVasya, userPetya));

        var resultDto = new ResponseUsers(true);
        resultDto.setUsers(userRepository.findAll().stream()
                .map(UserSimpleDto::fromUser).collect(Collectors.toList()));
        var expected = ResponseEntity.ok(resultDto);

        var result = userService.findAll();
        Assert.assertNotNull(result);
        assertNotNull(result.getBody());
        assertThat(result.getStatusCodeValue(), Is.is(200));
        assertEquals(expected.getBody(), result.getBody());
    }

    @Test
    public void testDeleteUser_success() {
        Long id = 1L;
        User user = createUserVasyaWithRoles();
        given(userRepository.findById(id)).willReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(id);
        var expected = ResponseEntity.ok(new BaseResponse(true));
        var actual = userService.deleteUser(id);
        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), Is.is(200));
        assertThat(actual.getBody(), Is.is(expected.getBody()));
    }

    @Test
    public void testDeleteUser_notFound() {
        Long id = 1L;
        given(userRepository.findById(id)).willReturn(Optional.empty());
        var expectedBody = new ResponseErrorsDto(false);
        expectedBody.addErrors("Пользователь не найден");
        var expected = ResponseEntity.badRequest().body(expectedBody);
        var actual = userService.deleteUser(id);
        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), Is.is(400));
        assertEquals(actual.getBody(), expected.getBody());
    }

    @Test
    public void testGetUserById_success() {
        User user = createUserVasyaWithRoles();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        var expectedBody = new ResponseUserWithRoles(true);
        expectedBody.setUser(UserRolesDto.fromUser(user));
        var expected = ResponseEntity.ok(expectedBody);
        var actual = userService.getUserById(user.getId());
        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), Is.is(200));
        assertThat(actual.getBody(), Is.is(expected.getBody()));
    }

    @Test
    public void testGetUserById_notFound() {
        Long id = 1L;
        given(userRepository.findById(id)).willReturn(Optional.empty());
        var expectedBody = new ResponseErrorsDto(false);
        expectedBody.addErrors("Пользователь не найден");
        var expected = ResponseEntity.badRequest().body(expectedBody);
        var actual = userService.getUserById(id);
        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), Is.is(400));
        assertEquals(actual.getBody(), expected.getBody());
    }

    @Test
    public void testAddUser_success() {
        User user = createUserVasyaWithRoles();
        UserRequestDto requestDto = new UserRequestDto(user.getName(), user.getLogin(),
                user.getPassword(), user.getRoles().stream()
                .map(Role::getId).collect(Collectors.toList()));
        given(userRepository.findUserByLogin(requestDto.getLogin())).willReturn(null);
        var expected = ResponseEntity.ok(new BaseResponse(true));
        var actual = userService.addUser(requestDto);
        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), Is.is(expected.getStatusCodeValue()));
        assertThat(actual.getBody(), Is.is(expected.getBody()));
    }

    @Test
    public void testAddUser_loginExists() {
        User user = createUserVasyaWithRoles();
        UserRequestDto requestDto = new UserRequestDto(user.getName(), user.getLogin(),
                user.getPassword(), user.getRoles().stream()
                .map(Role::getId).collect(Collectors.toList()));
        given(userRepository.findUserByLogin(requestDto.getLogin())).willReturn(user);
        String login = requestDto.getLogin();
        var expectedBody = new ResponseErrorsDto(false);
        expectedBody.addErrors("Такой логин " + login + " уже занят");
        var expected = ResponseEntity.badRequest().body(expectedBody);
        var actual = userService.addUser(requestDto);
        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), is(expected.getStatusCodeValue()));
        assertThat(actual.getBody(), is(expected.getBody()));
    }

    @Test
    public void testAddUser_wrongPasswordLoginExists() {
        User user = createUserVasyaWithRoles();
        UserRequestDto requestDto = new UserRequestDto(user.getName(), user.getLogin(),
                "pass", user.getRoles().stream()
                .map(Role::getId).collect(Collectors.toList()));
        String login = requestDto.getLogin();
        given(userRepository.findUserByLogin(login)).willReturn(user);

        var expectedBody = new ResponseErrorsDto(false);
        expectedBody.addErrors("Пароль не соответствует требованиям",
                "Такой логин " + login + " уже занят");
        var expected = ResponseEntity.badRequest().body(expectedBody);
        var actual = userService.addUser(requestDto);

        assertNotNull(actual);
        assertNotNull(actual.getBody());
        assertThat(actual.getStatusCodeValue(), is(expected.getStatusCodeValue()));
        assertThat(actual.getBody(), is(expected.getBody()));
    }
}
