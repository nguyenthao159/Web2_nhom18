package com.nguyenthithuthao.example05.payloads;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProductInCartDTO extends ProductDTO {
    private Integer cartQuantity;
    
    // getters và setters
    public Integer getCartQuantity() {
        return cartQuantity;
    }
    
    public void setCartQuantity(Integer cartQuantity) {
        this.cartQuantity = cartQuantity;
    }
}