package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.dto.UserReadOnlyDTO;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.Role;
import gr.aueb.cf.restbankapp.model.User;
import gr.aueb.cf.restbankapp.repository.RoleRepository;
import gr.aueb.cf.restbankapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class })
    public UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        try {
            if (userRepository.findByUsername(userInsertDTO.username()).isPresent()) {
                throw new EntityAlreadyExistsException("User", "User with username=" + userInsertDTO.username() + " already exists");
            }
            User user = mapper.mapToUserEntity(userInsertDTO);
            user.setPassword(passwordEncoder.encode(userInsertDTO.password()));
            Role role = roleRepository.findById(userInsertDTO.roleId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role", "Role id=" + userInsertDTO.roleId() + " invalid"));
            role.addUser(user);
            userRepository.save(user);
            log.info("Save succeeded for user with username={}.", userInsertDTO.username());
            return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityAlreadyExistsException e) {
            log.error("Save failed. User with username={} already exists", userInsertDTO.username());
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Save failed. Invalid arguments for user with username={}", userInsertDTO.username());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUUID(UUID uuid) throws EntityNotFoundException {
        try {
            User user = userRepository.findByUuid(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid=" + uuid + " not found"));
        log.debug("User with uuid={} found successfully", uuid);
        return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityNotFoundException e) {
            log.error("Get failed. User with uuid={} not found", uuid);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException {
        try {
            User user = userRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid=" + uuid + " not found"));
            log.debug("Active user with uuid={} found successfully", uuid);
            return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityNotFoundException e) {
            log.error("Get failed. User with uuid={} not found", uuid);
            throw e;
        }
    }
}