package ru.list.surkovr.services.interfaces;

import org.springframework.http.ResponseEntity;
import ru.list.surkovr.dto.requests.UserRequestDto;

public interface UserService {

    ResponseEntity findAll();

    ResponseEntity deleteUser(long id);

    ResponseEntity getUserById(long id);

    ResponseEntity addUser(UserRequestDto request);

    ResponseEntity editUser(UserRequestDto request, Long id);
}
