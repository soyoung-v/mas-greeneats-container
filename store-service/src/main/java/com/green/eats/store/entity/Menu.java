package com.green.eats.store.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Menu {
    @Id
    @Tsid // 오토인크리먼트 처럼 자동으로 pk를 만들어줌
    private Long id;

    @Column(nullable = false, length = 255)
    private  String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stockQuantity;

    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new RuntimeException("상품 '" + name + "'의 재고가 부족합니다. (현재: " + stockQuantity + ")");
        }
        this.stockQuantity = restStock;
    }

}
