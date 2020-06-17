package ru.list.surkovr.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.list.surkovr.model.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByLogin(String login);

    User findUserByName(String name);
}
