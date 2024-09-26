package gatedcommunity.repository;

import aj.org.objectweb.asm.commons.Remapper;
import gatedcommunity.model.entity.User ;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUserName(String userName);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);


}