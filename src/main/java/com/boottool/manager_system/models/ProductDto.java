package com.boottool.manager_system.models;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class ProductDto {

    private Long id;
    private LocalDateTime createdAt;
    private String imageFileName;

    /* ------------ VALIDATION ------------ */
    @NotBlank(message = "The name is required")
    private String name;

    @NotBlank(message = "The brand is required")
    private String brand;

    @NotBlank(message = "The category is required")
    private String category;

    @NotNull(message = "The price is required")
    @DecimalMin(value = "0.0", inclusive = true,
            message = "The price must be ≥ 0")
    private BigDecimal price;

    @Size(min = 10, max = 2000,
            message = "The description must be between 10 and 2000 characters")
    private String description;

    /* File upload (không đánh dấu @NotNull vì sẽ được kiểm tra thủ công) */
    private MultipartFile imageFile;

    /* ------------ GETTERS / SETTERS ------------ */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getImageFileName() { return imageFileName; }
    public void setImageFileName(String imageFileName) { this.imageFileName = imageFileName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }
}
