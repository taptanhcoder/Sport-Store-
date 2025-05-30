
package com.boottool.manager_system.services;

import com.boottool.manager_system.models.Inventory;
import com.boottool.manager_system.models.InventoryTransaction;
import com.boottool.manager_system.models.InventoryTxnType;
import com.boottool.manager_system.models.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    private final InventoryRepository invRepo;
    private final InventoryTransactionRepository txnRepo;

    public InventoryService(
            InventoryRepository invRepo,
            InventoryTransactionRepository txnRepo
    ) {
        this.invRepo = invRepo;
        this.txnRepo = txnRepo;
    }

    /** Lấy hoặc khởi tạo record Inventory cho product */
    @Transactional
    public Inventory getOrCreateInventory(Product product) {
        return invRepo.findByProductId(product.getId())
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProduct(product);
                    inv.setOnHand(0);
                    inv.setReserved(0);
                    inv.setUpdatedAt(LocalDateTime.now());
                    return invRepo.save(inv);
                });
    }

    /** Điều chỉnh tồn kho + ghi lịch sử giao dịch */
    @Transactional
    public void adjustStock(
            Product product,
            int delta,
            InventoryTxnType type,
            String note
    ) {
        Inventory inv = getOrCreateInventory(product);
        inv.setOnHand(inv.getOnHand() + delta);
        inv.setUpdatedAt(LocalDateTime.now());
        invRepo.save(inv);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setInventory(inv);
        txn.setType(type);
        txn.setQuantity(delta);
        txn.setNote(note);
        txn.setCreatedAt(LocalDateTime.now());
        txnRepo.save(txn);
    }

    /** Xuất hàng khi xác nhận đơn */
    public void consumeForOrder(Product product, int qty) {
        adjustStock(product, -qty, InventoryTxnType.OUT, "Consume for Order");
    }

    /** Trả hàng khi hủy đơn */
    public void releaseForOrder(Product product, int qty) {
        adjustStock(product, qty, InventoryTxnType.IN, "Release from cancelled Order");
    }
}
