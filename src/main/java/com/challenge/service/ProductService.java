package com.challenge.service;

import com.challenge.dto.product.DeleteProductDto;
import com.challenge.dto.product.ProductDto;
import com.challenge.dto.product.UpdateProductDto;
import com.challenge.entity.Product;
import com.challenge.entity.User;
import com.challenge.exception.ExceptionFactory;
import com.challenge.exception.exceptions.ResourceNotFoundException;
import com.challenge.mapper.ProductMapper;
import com.challenge.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * Service class for product related logic
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    /**
     * Error message keys
     */
    private static final String PRODUCT_NOT_FOUND = "exception.resourceNotFound.productNotFound";

    private final ProductRepository productRepository;
    private final UserService userService;
    private final ProductMapper productMapper;

    /**
     * Finds a product by its id. Uses pessimistic locking.
     */
    public Product getProductByIdLocked(UUID id) {
        return productRepository.findByIdPessimistic(id)
                .orElseThrow(() -> ExceptionFactory.create(ResourceNotFoundException.class, PRODUCT_NOT_FOUND));
    }

    /**
     * Fetches all the products.
     */
    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return productMapper.allEntitiesToDtos(products);
    }

    /**
     * Creates a new product. It sets the product's seller field
     * to the currently logged in user. It also validates the
     * product's cost field.
     */
    @Transactional
    public ProductDto createProduct(ProductDto newProductDto) {
        User loggedInUser = userService.getAuthenticatedUser();
        Product product = productMapper.dtoToEntity(newProductDto);
        product.setSeller(loggedInUser);

        productRepository.save(product);
        return productMapper.entityToDto(product);
    }

    /**
     * Updates a product if its owned by the currently logged
     * in user. It also validates the product's cost field.
     */
    @Transactional
    public ProductDto updateProduct(UpdateProductDto productDto) {
        Product productToUpdate = findProductByIdForCurrentSeller(productDto.getId());

        productToUpdate.setProductName(productDto.getProductName());
        productToUpdate.setAmountAvailable(productDto.getAmountAvailable());
        productToUpdate.setCost(productDto.getCost());

        return productMapper.entityToDto(productToUpdate);
    }

    /**
     * Removes a product if its owned by the currently logged in user.
     */
    @Transactional
    public void deleteProduct(DeleteProductDto productDto) {
        Product productToDelete = findProductByIdForCurrentSeller(productDto.getId());
        productRepository.deleteById(productToDelete.getId());
    }

    /**
     * Checks if the product we are looking for exists for the currently
     * logged in user. If not, a {@link ResourceNotFoundException} is thrown.
     *
     * @param productId is the product id we are looking for
     * @return the found product or throw exception
     */
    private Product findProductByIdForCurrentSeller(UUID productId) {
        User loggedInUser = userService.getAuthenticatedUser();
        return productRepository.findByIdAndSellerId(productId, loggedInUser.getId())
                .orElseThrow(() -> ExceptionFactory.create(ResourceNotFoundException.class, PRODUCT_NOT_FOUND));
    }

}
