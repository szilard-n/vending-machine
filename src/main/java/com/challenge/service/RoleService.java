package com.challenge.service;

import com.challenge.entity.Role;
import com.challenge.entity.RoleType;
import com.challenge.exception.ExceptionFactory;
import com.challenge.exception.exceptions.InvalidInputException;
import com.challenge.exception.exceptions.ResourceNotFoundException;
import com.challenge.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Service class for role related logic.
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    /**
     * Error message keys
     */
    private static final String ROLE_NOT_FOUND = "exception.resourceNotFound.roleNotFound";
    private static final String INVALID_ROLE = "exception.invalidInput.invalidRole";

    private final RoleRepository roleRepository;

    /**
     * Finds a role by its name if it exists
     */
    protected Role getRoleByName(String roleName) {
        return roleRepository.findByRoleType(validateAndGetRoleName(roleName))
                .orElseThrow(() -> ExceptionFactory.create(ResourceNotFoundException.class, ROLE_NOT_FOUND, roleName));
    }

    /**
     * Validates a roleName against {@link RoleType} and returns the
     * enum value if valid.
     */
    private RoleType validateAndGetRoleName(String roleName) {
        try {
            return RoleType.valueOf(roleName);
        } catch (IllegalArgumentException ex) {
            throw ExceptionFactory.create(InvalidInputException.class, INVALID_ROLE, Arrays.asList(RoleType.values()));
        }
    }
}
