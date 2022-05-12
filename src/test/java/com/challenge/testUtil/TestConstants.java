package com.challenge.testUtil;

import com.challenge.entity.Product;
import com.challenge.entity.Role;
import com.challenge.entity.RoleType;
import com.challenge.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Utility class for constant values used in tests
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestConstants {

    public static final User BUYER_1 = User.builder()
            .id(UUID.fromString("00000000-1000-0000-0000-000000000000"))
            .username("buyer")
            .password("password")
            .role(Role.builder()
                    .roleType(RoleType.ROLE_BUYER)
                    .build())
            .build();

    public static final User BUYER_2 = User.builder()
            .id(UUID.fromString("00000000-3000-0000-0000-000000000000"))
            .username("buyerWithMoney")
            .password("password")
            .role(Role.builder()
                    .roleType(RoleType.ROLE_BUYER)
                    .build())
            .build();

    public static final User SELLER_1 = User.builder()
            .id(UUID.fromString("00000000-2000-0000-0000-000000000000"))
            .username("seller")
            .password("password")
            .role(Role.builder()
                    .roleType(RoleType.ROLE_SELLER)
                    .build())
            .build();

    public static final User SELLER_2 = User.builder()
            .id(UUID.fromString("00000000-5000-0000-0000-000000000000"))
            .username("seller_2")
            .password("password")
            .role(Role.builder()
                    .roleType(RoleType.ROLE_SELLER)
                    .build())
            .build();

    public static final Product PRODUCT_1 = Product.builder()
            .id(UUID.fromString("00000000-1000-0000-0000-000000000000"))
            .productName("PRODUCT_1")
            .amountAvailable(100)
            .cost(10)
            .seller(SELLER_1)
            .build();

    public static final Product PRODUCT_2 = Product.builder()
            .id(UUID.fromString("00000000-2000-0000-0000-000000000000"))
            .productName("PRODUCT_2")
            .amountAvailable(10)
            .cost(100)
            .seller(SELLER_1)
            .build();

    public static final Product PRODUCT_4 = Product.builder()
            .id(UUID.fromString("00000000-4000-0000-0000-000000000000"))
            .productName("PRODUCT_4")
            .amountAvailable(100)
            .cost(5)
            .seller(SELLER_2)
            .build();
}
