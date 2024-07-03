package com.nhomconga.doanchieuthu4.controller;


import com.nhomconga.doanchieuthu4.model.Category;
import com.nhomconga.doanchieuthu4.model.Product;
import com.nhomconga.doanchieuthu4.service.CategoryService;
import com.nhomconga.doanchieuthu4.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Value("${upload.path}")
    private String uploadPath;

    @ModelAttribute("categories")
    public List<Category> populateCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping
    public String showProductList(Model model, @RequestParam(defaultValue = "0") int page) {
        int pageSize = 6; // Số sản phẩm mỗi trang
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage = productService.getAllProducts(pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentPage", page);

        return "products/product-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@Valid Product product, BindingResult result,
                             @RequestParam("images") MultipartFile imageFile, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/add-product";
        }
        try {
            if (!imageFile.isEmpty()) {
                String filename = imageFile.getOriginalFilename();
                Path path = Paths.get(uploadPath, filename);
                Files.write(path, imageFile.getBytes());
                product.setImage(filename);
                product.setImageUrl("/productImage/" + filename); // Thêm đường dẫn URL mới cho hình ảnh
            }
            productService.addProduct(product);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/add-product";
        }
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/update-product";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @Valid Product product,
                                BindingResult result, @RequestParam("images") MultipartFile imageFile, Model model) {
        if (result.hasErrors()) {
            product.setId(id);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/update-product";
        }
        try {
            // Kiểm tra xem có hình ảnh mới được chọn không
            if (!imageFile.isEmpty()) {
                String filename = imageFile.getOriginalFilename();
                Path path = Paths.get(uploadPath, filename);
                Files.write(path, imageFile.getBytes());
                product.setImage(filename);
                product.setImageUrl("/productImages/" + filename); // Thêm đường dẫn URL mới cho hình ảnh

                // Kiểm tra xem hình ảnh cũ có tồn tại không
                Product existingProduct = productService.getProductById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
                if (existingProduct.getImage() != null && !existingProduct.getImage().equals(filename)) {
                    // Hiển thị hình ảnh mới khi chưa tồn tại trong tệp
                    model.addAttribute("imageUrl", product.getImageUrl());
                }
            } else {
                // Nếu không có hình ảnh mới được chọn, giữ nguyên hình ảnh cũ
                Product existingProduct = productService.getProductById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
                product.setImage(existingProduct.getImage());
                product.setImageUrl(existingProduct.getImageUrl());
            }
            productService.updateProduct(product, imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/update-product";
        }
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Model model) {
        try {
            productService.deleteProductById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error deleting product: " + e.getMessage());
            return "error"; // Hoặc trả về trang hiển thị lỗi tùy chọn
        }
        return "redirect:/products";
    }

    @GetMapping("/detail/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        model.addAttribute("product", product);
        return "products/detail";
    }
    @GetMapping("/search")
    public String searchProduct(@RequestParam("keyword") String keyword,
                                @RequestParam(value = "category", required = false) Long categoryId,
                                @RequestParam(value = "categoryName", required = false) String categoryName,
                                Model model) {
        List<Product> searchResults;
        if (categoryId != null) {
            searchResults = productService.searchProductsByKeywordAndCategory(keyword, categoryId);
        } else if (categoryName != null && !categoryName.isEmpty()) {
            searchResults = productService.searchProductsByKeywordAndCategoryName(keyword, categoryName);
        } else {
            searchResults = productService.searchProducts(keyword);
        }
        model.addAttribute("products", searchResults);
        return "products/search";
    }
}
