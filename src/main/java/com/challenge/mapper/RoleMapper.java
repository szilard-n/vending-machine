package com.challenge.mapper;

import com.challenge.entity.Role;
import org.mapstruct.Mapper;

/**
 * Mapper used for getting a {@link Role}'s string value
 */
@Mapper
public interface RoleMapper {

    default String entityToString(Role role) {
        return role.getAuthority();
    }
}
