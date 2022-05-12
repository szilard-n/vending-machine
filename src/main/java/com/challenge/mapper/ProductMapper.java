package com.challenge.mapper;

import com.challenge.dto.product.ProductDto;
import com.challenge.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * Mapper for {@link ProductDto} and {@link Product}
 */
@Mapper
public interface ProductMapper {

    @Mappings({
            @Mapping(target = "seller.deposit", ignore = true),
            @Mapping(target = "seller.role", ignore = true),
            @Mapping(target = "seller.products", ignore = true)
    })
    ProductDto entityToDto(Product product);

    @Mappings({
            @Mapping(target = "seller.deposit", ignore = true),
            @Mapping(target = "seller.role", ignore = true),
            @Mapping(target = "seller.products", ignore = true)
    })
    Product dtoToEntity(ProductDto productDto);

    @Mappings({
            @Mapping(target = "seller.deposit", ignore = true),
            @Mapping(target = "seller.role", ignore = true),
            @Mapping(target = "seller.products", ignore = true)
    })
    List<ProductDto> allEntitiesToDtos(List<Product> products);
}
