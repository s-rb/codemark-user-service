package ru.list.surkovr.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.list.surkovr.dto.requests.UserRequestDto;
import ru.list.surkovr.dto.responses.*;
import ru.list.surkovr.model.entities.Role;
import ru.list.surkovr.model.entities.User;
import ru.list.surkovr.repositories.RoleRepository;
import ru.list.surkovr.repositories.UserRepository;
import ru.list.surkovr.services.interfaces.UserService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    public static final String REGEX_PASSWORD = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public ResponseEntity findAll() {
        ResponseUsers resultDto = new ResponseUsers(true);
        resultDto.setUsers(userRepository.findAll().stream()
                .map(UserSimpleDto::fromUser).collect(Collectors.toList()));
        return ResponseEntity.ok(resultDto);
    }

    @Override
    public ResponseEntity deleteUser(long id) {
        if (userRepository.findById(id).isEmpty()) {
            return getUserNotFoundResponse();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(new BaseResponse(true));
    }

    @Override
    public ResponseEntity getUserById(long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return getUserNotFoundResponse();
        } else {
            var resultDto = new ResponseUserWithRoles(true);
            resultDto.setUser(UserRolesDto.fromUser(user));
            return ResponseEntity.ok(resultDto);
        }
    }

    @Override
    public ResponseEntity addUser(UserRequestDto request) {
        String login = request.getLogin();
        String name = request.getName();
        String password = request.getPassword();
        List<Integer> roles = request.getRoles();

        var responseDto = checkAddEditRequest(request);
        boolean isLoginExists = userRepository.findUserByLogin(login) != null;
        if (responseDto != null) {
            if (isLoginExists) {
                responseDto.addErrors("Такой логин " + login + " уже занят");
            }
            return ResponseEntity.badRequest().body(responseDto);
        }
        if (isLoginExists) {
            responseDto = new ResponseErrorsDto(false);
            responseDto.addErrors("Такой логин " + login + " уже занят");
            return ResponseEntity.badRequest().body(responseDto);
        }

        User user = new User();
        user.setLogin(login);
        user.setName(name);
        user.setPassword(password);

        if (roles != null) {
            List<Role> userRoles = roles.stream()
                    .map(i -> roleRepository.findById(i).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!userRoles.isEmpty())
                user.setRoles(userRoles); // Убрать условие, если нужно создавать с пустыми ролями
        }

        userRepository.save(user);
        return ResponseEntity.ok(new BaseResponse(true));
    }

    @Override
    public ResponseEntity editUser(UserRequestDto request, Long id) {
        String login = request.getLogin();
        String name = request.getName();
        String password = request.getPassword();
        List<Integer> roles = request.getRoles();

        User currentUser = userRepository.findById(id).orElse(null);
        if (currentUser == null) return ResponseEntity.badRequest().body(
                new ResponseErrorsDto(false,
                        List.of("Пользователь с таким id " + id + " не существует")));

        User userByLogin = userRepository.findUserByLogin(login);
        var resultDto = checkAddEditRequest(request);
        boolean isLoginExists = userByLogin != null && userByLogin != currentUser;
        if (resultDto == null && isLoginExists) {
            resultDto = new ResponseErrorsDto(false);
        }
        if (resultDto != null) {
            if (isLoginExists) resultDto.addErrors(
                    "Пользователь с таким логином " + login + " уже существует");
            return ResponseEntity.badRequest().body(resultDto);
        }

        currentUser.setName(name);
        currentUser.setLogin(login);
        currentUser.setPassword(password);
        if (roles != null) {    // Добавить условие && !roles.isEmpty если не нужно задавать пустой список ролей
            List<Role> roleList = roles.stream()
                    .map(r -> roleRepository.findById(r).orElse(null))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            currentUser.setRoles(roleList);
        }
        userRepository.save(currentUser);
        return ResponseEntity.ok(new BaseResponse(true));
    }

    private ResponseEntity getUserNotFoundResponse() {
        var response = new ResponseErrorsDto(false);
        response.addErrors("Пользователь не найден");
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseErrorsDto checkAddEditRequest(UserRequestDto request) {
        String password = request.getPassword();
        boolean isPasswordValid = validatePassword(password);
        if (!isPasswordValid) {
            var resultDto = new ResponseErrorsDto(false);
            resultDto.addErrors("Пароль не соответствует требованиям");
            return resultDto;
        }
        return null;
    }

    private boolean validatePassword(String password) {
        return !password.isBlank() && password.matches(REGEX_PASSWORD);
    }
}