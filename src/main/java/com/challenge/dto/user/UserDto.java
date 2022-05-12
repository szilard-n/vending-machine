package com.challenge.dto.user;

import com.challenge.dto.product.ProductDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * DTO class for {@link com.challenge.entity.User}
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    @NotNull
    private UUID id;

    @NotNull
    @Size(min = 6, max = 20)
    private String username;

    private Integer deposit;

    @NotNull
    @Pattern(regexp = "(ROLE_[A-Z]+)\\w")
    private String role;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ProductDto> products;

}
