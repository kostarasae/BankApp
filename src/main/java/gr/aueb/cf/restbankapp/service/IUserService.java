package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.dto.UserReadOnlyDTO;

import java.util.UUID;

public interface IUserService {
    UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    UserReadOnlyDTO getUserByUUID(UUID uuid);
    UserReadOnlyDTO getUserByUUIDDeletedFalse(UUID uuid);
}
