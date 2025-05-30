package com.boottool.manager_system.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateOrderRequest {

    @NotEmpty(message = "Chưa có sản phẩm nào được chọn")
    @Valid
    private List<Item> items;

    public static class Item {
        @Min(value = 1, message = "productId không hợp lệ")
        private Long productId;

        @Min(value = 1, message = "Số lượng phải ≥ 1")
        private int quantity;

        // getters & setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    // getters & setters
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}