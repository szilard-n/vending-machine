package com.challenge.service;

import com.challenge.dto.user.UpdateUserDto;
import com.challenge.dto.user.UserDto;
import com.challenge.entity.Role;
import com.challenge.entity.User;
import com.challenge.exception.ExceptionFactory;
import com.challenge.exception.exceptions.PasswordMatchException;
import com.challenge.exception.exceptions.UsernameAlreadyExistsException;
import com.challenge.mapper.UserMapper;
import com.challenge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Service class for user related logic
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    /**
     * Error message keys
     */
    private static final String PASSWORDS_NOT_MATCH = "exception.passwordMatch.passwordsNotMatching";
    private static final String USERNAME_ALREADY_EXISTS = "exception.usernameInUse.usernameAlreadyInUse";
    private static final String USERNAME_NOT_FOUND = "exception.authentication.usernameNoFound";

    private final UserRepository userRepository;
    private final UserAuthenticationService userAuthenticationService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserMapper userMapper;

    /**
     * Method used by Spring Security for authentication. It finds
     * a user by its username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByUsername(username);
    }

    /**
     * Fetches the currently logged in user.
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) authentication.getPrincipal();
        return getUserByUsername(username);
    }

    /**
     * Creates a new user entry with roles.
     */
    @Transactional
    public UserDto createUser(String username, String password, String userRole) {
        if (userRepository.existsByUsername(username)) {
            throw ExceptionFactory.create(UsernameAlreadyExistsException.class, USERNAME_ALREADY_EXISTS);
        }

        Role role = roleService.getRoleByName(userRole);
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
        user.setRole(role);

        log.info("User with username: {} has been created", username);
        return userMapper.entityToDto(user);
    }

    /**
     * Fetches all the users.
     */
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.allEntitiesToDtos(users);
    }

    /**
     * Updates the user's details
     */
    @Transactional
    public UserDto updateUser(UpdateUserDto userDto) {
        if (userRepository.existsByUsername(userDto.getNewUsername())) {
            throw ExceptionFactory.create(UsernameAlreadyExistsException.class, USERNAME_ALREADY_EXISTS);
        }

        User user = getAuthenticatedUser();
        Role role = roleService.getRoleByName(userDto.getNewRole());

        // set new username and deactivate all access tokens for old one
        String oldUsername = user.getUsername();
        user.setUsername(userDto.getNewUsername());
        user.setRole(role);
        userAuthenticationService.deactivateAuthentication(oldUsername);

        return userMapper.entityToDto(user);
    }

    /**
     * Updates the current user's passwords of the oldPassword
     * field is a perfect match with the currently saved password.
     */
    @Transactional
    public void updateUserPassword(String oldPassword, String newPassword) {
        User user = getAuthenticatedUser();

        // check if oldPassword matches the currently saved password of the user
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userAuthenticationService.deactivateAuthentication(user.getUsername());
        } else {
            throw ExceptionFactory.create(PasswordMatchException.class, PASSWORDS_NOT_MATCH);
        }
    }

    /**
     * Delete the current user.
     */
    @Transactional
    public void deleteUser() {
        User user = getAuthenticatedUser();
        userRepository.deleteById(user.getId());
        userAuthenticationService.deactivateAuthentication(user.getUsername());
    }

    /**
     * Deactivates all tokens for the current user.
     */
    public void logoutAuthenticatedUserFromAllSessions() {
        User user = getAuthenticatedUser();
        userAuthenticationService.deactivateAuthentication(user.getUsername());
    }

    /**
     * Finds a user by its username.
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ExceptionFactory.create(UsernameNotFoundException.class, USERNAME_NOT_FOUND, username));
    }

}
