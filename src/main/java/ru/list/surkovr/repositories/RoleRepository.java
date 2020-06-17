package ru.list.surkovr.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.list.surkovr.model.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findRoleByName(String name);
}