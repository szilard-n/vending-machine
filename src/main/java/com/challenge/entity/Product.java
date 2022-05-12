package com.challenge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Entity class representing a product
 */
@Entity
@Table(name = "T_PRODUCT")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(
            name = "ID",
            updatable = false
    )
    private UUID id;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "AMOUNT_AVAILABLE")
    private int amountAvailable;

    @Column(name = "COST")
    private int cost;

    @ManyToOne
    @JoinColumn(name = "SELLER_ID")
    private User seller;

    public int subtractFromAmountAvailable(int amount) {
        amountAvailable = amountAvailable - amount;
        return amountAvailable;
    }
}
