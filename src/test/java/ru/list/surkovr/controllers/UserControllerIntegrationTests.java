package ru.list.surkovr.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.list.surkovr.TestUtils;
import ru.list.surkovr.dto.requests.UserRequestDto;
import ru.list.surkovr.dto.responses.*;
import ru.list.surkovr.model.entities.Role;
import ru.list.surkovr.model.entities.User;
import ru.list.surkovr.repositories.RoleRepository;
import ru.list.surkovr.repositories.UserRepository;
import ru.list.surkovr.services.interfaces.UserService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = UserControllerIntegrationTests.UserControllerIntegTestConfig.class)
public class UserControllerIntegrationTests {

    @Configuration
    @ComponentScan("ru.list.surkovr")
    public static class UserControllerIntegTestConfig {

        @Bean
        @Autowired
        public SpringLiquibase springLiquibase(DataSource dataSource) throws SQLException {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDropFirst(true);
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog("classpath:liquibase/db.changelog-master.xml");
            return liquibase;
        }
    }

    public static final String URL_API_USERS = "/api/users/";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private User existedUser;

    private ObjectMapper mapper;

    @Before
    public void init() {
        existedUser = userRepository.findById(1L).orElse(null);
    }

    @Test
    public void testGetUsers_success() throws Exception {
        List<User> userList = userRepository.findAll();
        var expected = new ResponseUsers(true);
        expected.setUsers(userList.stream()
                .map(UserSimpleDto::fromUser).collect(Collectors.toList()));
        testPerformRequest(URL_API_USERS + "list", HttpMethod.GET, null,
                expected, status().isOk(), null);
    }

    @Test
    public void testGetUser_success() throws Exception {
        long id = 1L;
        var expected = new ResponseUserWithRoles(true);
        expected.setUser(UserRolesDto.fromUser(existedUser));
        testPerformRequest(URL_API_USERS + id, HttpMethod.GET, null,
                expected, status().isOk(), null);
    }

    @Test
    public void testDeleteUser_success() throws Exception {
        User userPetya = createUser();
        Long id = userRepository.save(userPetya).getId();

        assertNotNull(userRepository.findById(id).orElse(null));
        var expected = new BaseResponse(true);

        ResultActions result = mockMvc.perform(delete(URL_API_USERS + id).contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
        assertNull(userRepository.findById(id).orElse(null));
    }

    @Test
    public void testAddUser_success() throws Exception {
        User user = createUser();
        UserRequestDto request = new UserRequestDto(user.getName(),
                user.getLogin() + "_super", user.getPassword(),
                user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        var expected = new BaseResponse(true);
        mapper = new ObjectMapper();
        assertNull(userRepository.findUserByLogin(request.getLogin()));
        ResultActions result = mockMvc.perform(post(URL_API_USERS + "add").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request)));
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
        User savedUser = userRepository.findUserByLogin(request.getLogin());
        assertNotNull(savedUser);
        assertThat(savedUser.getLogin(), is(request.getLogin()));
        assertThat(savedUser.getName(), is(request.getName()));
        assertThat(savedUser.getPassword(), is(request.getPassword()));
        assertThat(savedUser.getRoles().stream().map(Role::getId)
                .collect(Collectors.toList()), is(request.getRoles()));
        assertNotNull(savedUser.getId());
    }

    @Test
    public void testAddUser_emptyParams() throws Exception {
        UserRequestDto request = new UserRequestDto(" ",
                null, "");
        var expected = new ResponseErrorsDto(false);
        expected.addErrors("Отсутствует логин",
                "Отсутствует имя", "Отсутствует пароль");
        testPerformRequest(URL_API_USERS + "add", HttpMethod.POST, request,
                expected, status().isBadRequest(), null);
    }

    @Test
    public void testAddUser_wrongPassword_loginExists() throws Exception {
        User user = createUser();
        UserRequestDto request = new UserRequestDto(user.getName(),
                existedUser.getLogin(), "pass",
                user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        var expected = new ResponseErrorsDto(false);
        expected.addErrors("Пароль не соответствует требованиям",
                "Такой логин " + request.getLogin() + " уже занят");
        assertNotNull(userRepository.findUserByLogin(request.getLogin()));
        testPerformRequest(URL_API_USERS + "add", HttpMethod.POST, request,
                expected, status().isBadRequest(), null);
        assertNull(userRepository.findUserByName(request.getName()));
    }

    @Test
    public void testEditUser_success() throws Exception {
        User user = createUser();
        User savedUser = userRepository.save(user);
        Long id = savedUser.getId();
        assertNotNull(userRepository.findById(id));
        assertThat(savedUser.getLogin(), is(user.getLogin()));

        UserRequestDto request = new UserRequestDto(user.getName() + "A",
                user.getLogin() + "A", user.getPassword() + "A",
                List.of(1));
        var expected = new BaseResponse(true);
        mapper = new ObjectMapper();
        ResultActions result = mockMvc.perform(put(URL_API_USERS + "edit/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request)));
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));

        User updatedUser = userRepository.findById(id).orElse(null);
        assertNotNull(updatedUser);
        assertThat(updatedUser.getLogin(), is(request.getLogin()));
        assertThat(updatedUser.getName(), is(request.getName()));
        assertThat(updatedUser.getPassword(), is(request.getPassword()));
        assertThat(updatedUser.getRoles().stream().map(Role::getId)
                .collect(Collectors.toList()), is(request.getRoles()));
    }

    @Test
    public void testEditUser_emptyParams() throws Exception {
        long id = 1;
        UserRequestDto request = new UserRequestDto(" ",
                null, "");
        var expected = new ResponseErrorsDto(false);
        expected.addErrors("Отсутствует логин",
                "Отсутствует имя", "Отсутствует пароль");
        testPerformRequest(URL_API_USERS + "edit/" + id, HttpMethod.PUT, request,
                expected, status().isBadRequest(), null);
    }

    @Test
    public void testEditUser_notFoundId() throws Exception {
        long id = -1;
        User user = createUser();
        UserRequestDto request = new UserRequestDto(user.getName() + "A",
                user.getLogin() + "A", user.getPassword() + "A",
                List.of(1));
        var expected = new ResponseErrorsDto(false,
                List.of("Пользователь с таким id " + id + " не существует"));
        testPerformRequest(URL_API_USERS + "edit/" + id, HttpMethod.PUT, request,
                expected, status().isBadRequest(), null);
    }

    @Test
    public void testEditUser_wrongPassword_loginAlreadyExists() throws Exception {
        User user = createUser();
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser);
        assertNotEquals(savedUser.getId(), existedUser.getId());
        assertNotEquals(savedUser.getLogin(), existedUser.getLogin());
        long id = savedUser.getId();

        UserRequestDto request = new UserRequestDto(user.getName(),
                existedUser.getLogin(), "pass",
                user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        var expected = new ResponseErrorsDto(false);
        expected.addErrors("Пароль не соответствует требованиям",
                "Пользователь с таким логином " + request.getLogin() + " уже существует");
        testPerformRequest(URL_API_USERS + "edit/" + id,
                HttpMethod.PUT, request,
                expected, status().isBadRequest(), null);
        User currentUser = userRepository.findById(id).orElse(null);
        assertNotNull(currentUser);
        assertThat(savedUser.toString(), is(currentUser.toString()));
    }

    @Test
    public void testGetUser_notFoundId() throws Exception {
        long id = -1;
        var expected = new ResponseErrorsDto(false);
        expected.addErrors("Пользователь не найден");
        testPerformRequest(URL_API_USERS + id, HttpMethod.GET, null,
                expected, status().isBadRequest(), null);
    }

    @Test
    public void testDeleteUser_notFoundId() throws Exception {
        long id = -1;
        var expected = new ResponseErrorsDto(false);
        expected.addErrors("Пользователь не найден");
        testPerformRequest(URL_API_USERS + id, HttpMethod.DELETE, null,
                expected, status().isBadRequest(), null);
    }

    private User createUser() {
        User userPetya = TestUtils.createUserPetyaWithRoles();
        Role role1 = roleRepository.getOne(1);
        Role role2 = roleRepository.getOne(2);
        userPetya.setRoles(List.of(role1, role2));
        return userPetya;
    }


    private void testPerformRequest(String url, HttpMethod httpMethod, Object request,
                                    Object expectedResponse, ResultMatcher expectedStatus,
                                    Map<String, String> params) throws Exception {
        mapper = new ObjectMapper();
        ResultActions result = null;
        MockHttpServletRequestBuilder requestBuilders = null;
        switch (httpMethod) {
            case GET:
                requestBuilders = get(url);
                break;
            case PUT:
                requestBuilders = put(url);
                break;
            case POST:
                requestBuilders = post(url);
                break;
        }
        if (requestBuilders == null) return;
        if ((params == null || params.isEmpty()) && request != null) {
            requestBuilders = requestBuilders.contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsBytes(request));
        } else if ((params != null && !params.isEmpty()) && request != null) {
            params.forEach(requestBuilders::param);
            requestBuilders.contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsBytes(request));
        } else if (params != null && !params.isEmpty()) {
            MockHttpServletRequestBuilder finalRequestBuilders = requestBuilders;
            params.entrySet().forEach(i -> finalRequestBuilders.param(i.getKey(), i.getValue()));
        }
        result = mockMvc.perform(requestBuilders);
        result.andExpect(expectedStatus);
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }
}
