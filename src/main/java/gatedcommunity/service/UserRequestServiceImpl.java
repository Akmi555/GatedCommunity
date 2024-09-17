package gatedcommunity.service;

import gatedcommunity.exception_handling.exceptions.TextException;
import gatedcommunity.model.dto.UserRequestDTO;
import gatedcommunity.model.entity.PropositionService;
import gatedcommunity.model.entity.UserRequest;
import gatedcommunity.repository.UserRequestRepository;
import gatedcommunity.service.interfaces.UserRequestService;
import gatedcommunity.service.mapping.UserRequestMappingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRequestServiceImpl implements UserRequestService {

    private final UserRequestRepository repository;
    private final UserRequestMappingService mapper;

    public UserRequestServiceImpl(UserRequestRepository repository, UserRequestMappingService mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public void attachPhoto(String photoUrl, String description) {

    }

    @Override
    public UserRequestDTO saveUserRequest(UserRequestDTO userRequestDTO) {
        UserRequest userRequest = mapper.mapDTOToEntity(userRequestDTO);
        userRequest.setActive(true);
        return mapper.mapEntityToDTO(repository.save(userRequest));

    }

    @Override
    public UserRequestDTO getUserRequestById(long id) {
        UserRequest userRequest = repository.findById(id).orElse(null);
        if (userRequest == null) {
            throw new TextException("User_Request with id " + id + " not found");
        }
        if (!userRequest.isActive()) {
            System.out.println("User_Request not active");
            throw new TextException("User_Request with id " + id + " not active"); // подумать
        }
        return mapper.mapEntityToDTO(userRequest);

    }

    @Override
    public List<UserRequestDTO> getAllUserRequest() {

        return repository.findAll().stream()
                .filter(UserRequest::isActive)
                .map(mapper::mapEntityToDTO)
                .toList();
    }

    @Override
    public UserRequestDTO updateUserRequest(Long id, UserRequestDTO userRequestDTO) {
        return null;
    }

    @Override
    public UserRequestDTO deleteUserRequestById(Long id) {
        UserRequest userRequest = repository.findById(id).orElse(null);
        if (userRequest != null) {
            repository.deleteById(id);
        }

        return mapper.mapEntityToDTO(userRequest);
    }

    @Override
    public UserRequestDTO restoreUserRequestById(Long id) {
        UserRequest userRequest = repository.findById(id).orElse(null);
        if (userRequest != null) {
            userRequest.setActive(true);
        }

        return mapper.mapEntityToDTO(userRequest);
    }

    @Override
    public UserRequestDTO removeUserRequestById(Long id) {
        UserRequest userRequest = repository.findById(id).orElse(null);
        if (userRequest != null) {
            userRequest.setActive(false);
        }
        return mapper.mapEntityToDTO(userRequest);
    }
}
