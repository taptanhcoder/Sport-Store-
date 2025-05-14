package com.boottool.manager_system.controllers;

import com.boottool.manager_system.models.Product;
import com.boottool.manager_system.models.ProductDto;
import com.boottool.manager_system.services.ProductsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String createProductForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        return "products/CreateProduct";
    }



    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("productDto") ProductDto productDto,
            BindingResult result
    ) {

        if (productDto.getImageFile() == null || productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
        }

        if (result.hasErrors()) {
            System.out.println(result.getAllErrors());
            return "products/CreateProduct";
        }


        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "D:/manager_system/public/image/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("Image saved to: " + uploadPath.resolve(storageFileName));

        } catch (Exception ex) {
            System.out.println("File upload exception: " + ex.getMessage());
        }


        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        System.out.println("Saving product to DB: " + product.getName());
        repo.save(product);
        System.out.println("Save success.");

        return "redirect:/products";
    }


    @GetMapping("/edit")
    public String editProductForm(@RequestParam int id, Model model) {
        Product product = repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setBrand(product.getBrand());
        productDto.setCategory(product.getCategory());
        productDto.setPrice(product.getPrice());
        productDto.setDescription(product.getDescription());
        productDto.setImageFileName(product.getImageFileName());
        productDto.setCreatedAt(product.getCreatedAt());

        model.addAttribute("productDto", productDto);

        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(
            @RequestParam int id,
            @Valid @ModelAttribute("productDto") ProductDto productDto,
            BindingResult result,
            Model model
    ) {
        Product product = repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        if (result.hasErrors()) {

            productDto.setImageFileName(product.getImageFileName());
            productDto.setCreatedAt(product.getCreatedAt());

            model.addAttribute("productDto", productDto);
            return "products/EditProduct";
        }


        if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
            String uploadDir = "D:/manager_system/public/image/";
            String oldFileName = product.getImageFileName();
            Path oldImagePath = Paths.get(uploadDir, oldFileName);

            try {
                Files.deleteIfExists(oldImagePath);
            } catch (Exception ex) {
                System.out.println("Delete old image failed: " + ex.getMessage());
            }

            MultipartFile newImage = productDto.getImageFile();
            String newFileName = System.currentTimeMillis() + "_" + newImage.getOriginalFilename();

            try (InputStream inputStream = newImage.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir, newFileName),
                        StandardCopyOption.REPLACE_EXISTING);
                product.setImageFileName(newFileName);
            } catch (Exception ex) {
                System.out.println("Upload new image failed: " + ex.getMessage());
            }
        }

        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());

        repo.save(product);

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Product product = repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

            Path imagePath = Paths.get("D:/manager_system/public/image/", product.getImageFileName());
            try {
                Files.deleteIfExists(imagePath);
            } catch (Exception ex) {
                System.out.println("Delete image failed: " + ex.getMessage());
            }

            repo.delete(product);
        } catch (Exception ex) {
            System.out.println("Delete product failed: " + ex.getMessage());
        }

        return "redirect:/products";
    }



}


