package com.challenge.mapper;

import com.challenge.dto.product.ProductDto;
import com.challenge.dto.user.UserDto;
import com.challenge.entity.Product;
import com.challenge.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for {@link UserDto} and {@link User}
 */
@Mapper(
        uses = {RoleMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface UserMapper {

    UserDto entityToDto(User user);

    List<UserDto> allEntitiesToDtos(List<User> users);

    /**
     * We need this method so that we are able to ignore
     * the nested seller field inside the user's products
     * (bidirectional binding between a {@link User} and {@link Product}).
     */
    @Mapping(target = "seller", ignore = true)
    ProductDto mapProductToDto(Product products);
}
