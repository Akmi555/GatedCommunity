package gatedcommunity.service;

import gatedcommunity.model.dto.UserDTO;
import gatedcommunity.model.dto.UserRegisterDTO;
import gatedcommunity.model.entity.ConfirmationCode;
import gatedcommunity.model.entity.User;
import gatedcommunity.repository.UserRepository;
import gatedcommunity.service.interfaces.ConfirmationCodeService;
import gatedcommunity.service.interfaces.EmailService;
import gatedcommunity.service.interfaces.RoleService;
import gatedcommunity.service.interfaces.UserService;
import gatedcommunity.service.mapping.UserMappingService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final EmailService emailService;
    private final UserMappingService mapper;
    private final ConfirmationCodeService confirmationCodeService;

    public UserServiceImpl(UserRepository repository, BCryptPasswordEncoder passwordEncoder, RoleService roleService, EmailService emailService, UserMappingService mapper, ConfirmationCodeService confirmationCodeService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.emailService = emailService;
        this.mapper = mapper;
        this.confirmationCodeService = confirmationCodeService;
    }


    @Transactional
    @Override
    public void register(UserRegisterDTO registerDTO) {
        User user = mapper.mapRegisterDTOToEntity(registerDTO);

        Optional<User> optionalUser = repository.findByEmail(user.getEmail());
        if (optionalUser.isPresent() && optionalUser.get().isActive()) {
            throw new RuntimeException("Email " + user.getEmail() + " is already in use");
        }

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            ConfirmationCode codeOld = confirmationCodeService.findCodeByUser(user).orElse(null);
            if (codeOld != null) {
                confirmationCodeService.remove(codeOld);
            }
        } else {
            user.setRoles(Set.of(roleService.getRoleUser()));
        }
        user.setPassword(passwordEncoder.encode(registerDTO.password()));

        user.setActive(false);

        repository.save(user);

        emailService.sendConfirmationEmail(user);

    }

    @Transactional
    @Override
    public String confirmationMailByCode(String code) {
        ConfirmationCode confirmationCode = confirmationCodeService.findByCode(code).orElseThrow(
                () -> new RuntimeException("Code not found")
        );

        if (confirmationCode.getExpired().isAfter(LocalDateTime.now())) {
            User user = confirmationCode.getUser();
            user.setActive(true);
            repository.save(user);
            return user.getEmail() + " confirmed!";
        }

        throw new RuntimeException("Wrong code");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findUserByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public UserDTO saveUser(UserDTO userDTO) {
        User user = mapper.mapDTOToEntity(userDTO);
        user.setActive(true);
        return mapper.mapEntityToDTO(repository.save(user));

    }

    @Override
    public List<UserDTO> getAllUsers() {
        return repository.findAll().stream()
                .map(mapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(long id) {
        return repository.findById(id)
                .map(mapper::mapEntityToDTO)
                .orElseThrow(() -> new RuntimeException("The user with the ID was not found: " + id));
    }

    @Override
    public UserDTO getUserByName(String name) {
        return repository.findUserByUserName(name)
                .map(mapper::mapEntityToDTO)
                .orElseThrow(() -> new RuntimeException("User not found: " + name));
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        mapper.mapDTOToEntityUpdate(userDTO, user);
        user.setActive(true);
        return mapper.mapEntityToDTO(repository.save(user));
    }

    @Override
    public UserDTO deleteUserById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        repository.deleteById(id);
        return mapper.mapEntityToDTO(user);
    }

    @Override
    public UserDTO restoreUserById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setActive(true);
        return mapper.mapEntityToDTO(repository.save(user));
    }

    @Override
    public UserDTO removeUserById(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(false);
        return mapper.mapEntityToDTO(repository.save(user));

    }
}
