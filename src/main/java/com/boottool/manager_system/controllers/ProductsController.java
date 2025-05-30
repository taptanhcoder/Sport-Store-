package com.boottool.manager_system.controllers;

import com.boottool.manager_system.models.Product;
import com.boottool.manager_system.models.ProductDto;
import com.boottool.manager_system.services.ProductsRepository;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductsController {

    private static final DateTimeFormatter FILE_TS =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ProductsRepository repo;
    private final Path uploadPath;

    public ProductsController(
            ProductsRepository repo,
            @Value("${app.upload.dir}") String uploadDir
    ) {
        this.repo = repo;
        this.uploadPath = Paths.get(uploadDir);
    }

    /** ➊ KHAI BÁO CONTEXT CHO MỌI REQUEST — GIÚP IDE HIỂU */
    @ModelAttribute("productDto")
    public ProductDto productDto() {
        return new ProductDto();
    }

    /* ---------- LIST ---------- */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("products",
                repo.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "products/index";
    }

    /* ---------- CREATE (GET) ---------- */
    @GetMapping("/create")
    public String showCreate() {
        return "products/create";   // Không cần addAttribute vì @ModelAttribute đã làm
    }

    /* ---------- CREATE (POST) ---------- */
    @PostMapping("/create")
    public String doCreate(
            @Valid @ModelAttribute("productDto") ProductDto dto,
            BindingResult result,
            RedirectAttributes flash
    ) {

        /* kiểm tra file bắt buộc */
        if (dto.getImageFile() == null || dto.getImageFile().isEmpty()) {
            result.rejectValue("imageFile", null,
                    "Ảnh sản phẩm là bắt buộc");
        }

        if (result.hasErrors()) {
            return "products/create";
        }

        try {
            MultipartFile file = dto.getImageFile();
            String filename = LocalDateTime.now().format(FILE_TS)
                    + "_" + sanitize(file.getOriginalFilename());

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, uploadPath.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            /* map DTO → Entity */
            Product product = new Product();
            product.setName(dto.getName());
            product.setBrand(dto.getBrand());
            product.setCategory(dto.getCategory());
            product.setPrice(dto.getPrice());
            product.setDescription(dto.getDescription());
            product.setCreatedAt(LocalDateTime.now());
            product.setImageFileName(filename);
            repo.save(product);

            flash.addFlashAttribute("success", "Tạo sản phẩm thành công!");
            return "redirect:/products";

        } catch (Exception ex) {
            logger.error("Lỗi khi tạo sản phẩm", ex);
            flash.addFlashAttribute("error",
                    "Không thể tạo sản phẩm: " + ex.getMessage());
            return "redirect:/products/create";
        }
    }

    /* ---------- EDIT (GET) ---------- */
    @GetMapping("/{id}/edit")
    public String showEdit(@PathVariable Long id, Model model) {
        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy sản phẩm #" + id));

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setImageFileName(product.getImageFileName());

        model.addAttribute("productDto", dto);
        return "products/edit";
    }

    /* ---------- EDIT (POST) ---------- */
    @PostMapping("/{id}/edit")
    public String doEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("productDto") ProductDto dto,
            BindingResult result,
            RedirectAttributes flash
    ) {
        if (result.hasErrors()) {
            return "products/edit";
        }

        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy sản phẩm #" + id));

        try {
            /* xử lý ảnh mới (nếu có) */
            if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
                if (product.getImageFileName() != null) {
                    Files.deleteIfExists(uploadPath.resolve(product.getImageFileName()));
                }
                MultipartFile file = dto.getImageFile();
                String filename = LocalDateTime.now().format(FILE_TS)
                        + "_" + sanitize(file.getOriginalFilename());
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, uploadPath.resolve(filename),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(filename);
            }

            /* cập nhật field khác */
            product.setName(dto.getName());
            product.setBrand(dto.getBrand());
            product.setCategory(dto.getCategory());
            product.setPrice(dto.getPrice());
            product.setDescription(dto.getDescription());

            repo.save(product);
            flash.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
            return "redirect:/products";

        } catch (Exception ex) {
            logger.error("Lỗi khi cập nhật sản phẩm", ex);
            flash.addFlashAttribute("error",
                    "Không thể cập nhật sản phẩm: " + ex.getMessage());
            return "redirect:/products/" + id + "/edit";
        }
    }

    /* ---------- DELETE ---------- */
    @GetMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes flash
    ) {
        Optional<Product> opt = repo.findById(id);
        if (opt.isEmpty()) {
            flash.addFlashAttribute("error",
                    "Không tìm thấy sản phẩm với ID: " + id);
            return "redirect:/products";
        }

        try {
            Product p = opt.get();
            if (p.getImageFileName() != null &&
                    !p.getImageFileName().isBlank()) {
                Files.deleteIfExists(uploadPath.resolve(p.getImageFileName()));
            }
            repo.delete(p);
            flash.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (DataIntegrityViolationException ex) {
            logger.error("Lỗi ràng buộc dữ liệu khi xóa sản phẩm", ex);
            flash.addFlashAttribute("error",
                    "Không thể xóa: sản phẩm đang được tham chiếu.");
        } catch (Exception ex) {
            logger.error("Lỗi khi xóa sản phẩm", ex);
            flash.addFlashAttribute("error",
                    "Xảy ra lỗi: " + ex.getMessage());
        }
        return "redirect:/products";
    }

    /* ---------- TIỆN ÍCH ---------- */
    /** Loại bỏ ký tự nguy hiểm khỏi tên file. */
    private static String sanitize(String original) {
        return original == null ? "file"
                : original.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
