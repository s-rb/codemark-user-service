package ru.list.surkovr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.list.surkovr.dto.requests.UserRequestDto;
import ru.list.surkovr.dto.responses.ResponseErrorsDto;
import ru.list.surkovr.services.interfaces.UserService;

@RestController
@RequestMapping("/api/users/")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("list")
    public ResponseEntity getUsers() {
        return userService.findAll();
    }

    @GetMapping("{id}")
    public ResponseEntity getUser(@PathVariable("id") long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteUser(@PathVariable("id") long id) {
        return userService.deleteUser(id);
    }

    @PostMapping("add")
    public ResponseEntity addUser(@RequestBody UserRequestDto request) {
        var resultDto = checkAddEditRequest(request);
        if (resultDto != null) return ResponseEntity.badRequest().body(resultDto);
        return userService.addUser(request);
    }

    @PutMapping("edit/{id}")
    public ResponseEntity editUser(@RequestBody UserRequestDto request,
                                   @PathVariable("id") long id) {
        var resultDto = checkAddEditRequest(request);
        if (resultDto != null) {
            return ResponseEntity.badRequest().body(resultDto);
        }
        return userService.editUser(request, id);
    }

    private ResponseErrorsDto checkAddEditRequest(UserRequestDto request) {
        String login = request.getLogin();
        String name = request.getName();
        String password = request.getPassword();
        if (login == null || name == null || password == null ||
                login.isBlank() || name.isBlank() || password.isBlank()) {
            var response = new ResponseErrorsDto(false);
            response.addErrors(
                    (login == null || login.isBlank()) ? "Отсутствует логин" : null,
                    (name == null || name.isBlank()) ? "Отсутствует имя" : null,
                    (password == null || password.isBlank()) ? "Отсутствует пароль" : null);
            return response;
        }
        return null;
    }
}
