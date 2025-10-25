package com.farmatodo.product.service;

import com.farmatodo.product.domain.Product;
import com.farmatodo.product.dto.CreateProductRequest;
import com.farmatodo.product.dto.UpdateProductRequest;
import com.farmatodo.product.dto.ProductDTO;
import com.farmatodo.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    @Value("${product.min-stock-visible:1}")
    private int minStockVisible;

    /** -------- CREATE -------- */
    @Transactional
    public ProductDTO create(CreateProductRequest req) {
        //  validar duplicado por nombre
        if (repo.existsByNameIgnoreCase(req.name())) throw conflict("Product name already exists");

        Product p = repo.save(Product.builder()
                .name(req.name())
                .price(req.price())
                .stock(req.stock())
                .build());
        return map(p);
    }

    /** -------- READ -------- */
    @Transactional(readOnly = true)
    public ProductDTO getById(Long id){
        var p = repo.findById(id).orElseThrow(() -> notFound("Product not found"));
        return map(p);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> list(){
        return repo.findAll().stream().map(this::map).toList();
    }

    /** -------- UPDATE (PUT) -------- */
    @Transactional
    public ProductDTO update(Long id, UpdateProductRequest req) {
        var p = repo.findById(id).orElseThrow(() -> notFound("Product not found"));
        p.setName(req.name());
        p.setPrice(req.price());
        p.setStock(req.stock());
        return map(repo.save(p));
    }

    /** -------- DELETE -------- */
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw notFound("Product not found");
        repo.deleteById(id);
    }

    @Transactional
    public void decrement(Long id, int qty){
        var p = repo.findById(id).orElseThrow(() -> notFound("Product not found"));
        if (qty <= 0) throw badRequest("qty must be > 0");
        if (p.getStock() < qty) throw conflict("Insufficient stock");
        p.setStock(p.getStock() - qty);
        repo.save(p);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> search(String q){
        String s = (q==null? "" : q);
        return repo.findByNameContainingIgnoreCaseAndStockGreaterThanEqual(s, minStockVisible)
                .stream().map(this::map).toList();
    }

    /** -------- helpers -------- */
    private ProductDTO map(Product p){ return new ProductDTO(p.getId(), p.getName(), p.getPrice(), p.getStock()); }

    private ErrorResponseException notFound(String m){ var ex=new ErrorResponseException(HttpStatus.NOT_FOUND); ex.setDetail(m); return ex; }
    private ErrorResponseException badRequest(String m){ var ex=new ErrorResponseException(HttpStatus.BAD_REQUEST); ex.setDetail(m); return ex; }
    private ErrorResponseException conflict(String m){ var ex=new ErrorResponseException(HttpStatus.CONFLICT); ex.setDetail(m); return ex; }
}
