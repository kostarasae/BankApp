package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.dto.UserReadOnlyDTO;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    UserReadOnlyDTO getUserByUUID(UUID uuid) throws EntityNotFoundException;
    UserReadOnlyDTO getUserByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException;
    List<UserReadOnlyDTO> getStaffUsers();
    void deleteUser(UUID uuid) throws EntityNotFoundException, EntityInvalidArgumentException;
    void resetPassword(UUID uuid, String newPassword) throws EntityNotFoundException;
    void changePassword(String uuid, String currentPassword, String newPassword)
            throws EntityNotFoundException, EntityInvalidArgumentException;
    boolean isUserExists(String username);
}
