package com.boottool.manager_system.models;

/**
 * Kiểu giao dịch kho: nhập, xuất, điều chỉnh, giữ, giải phóng.
 */
public enum InventoryTxnType {
    IN,         // Nhập kho
    OUT,        // Xuất kho
    ADJUSTMENT, // Điều chỉnh kho
    RESERVE,    // Giữ hàng
    RELEASE     // Giải phóng giữ hàng
}