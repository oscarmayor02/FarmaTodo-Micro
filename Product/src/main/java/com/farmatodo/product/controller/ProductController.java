// com.farmatodo.product.controller.ProductController

package com.farmatodo.product.controller;

import com.farmatodo.product.dto.CreateProductRequest;
import com.farmatodo.product.dto.UpdateProductRequest;
import com.farmatodo.product.dto.ProductDTO;
import com.farmatodo.product.service.ProductService;
import com.farmatodo.product.service.SearchLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final SearchLogService searchLogService;

    /** -------- CRUD -------- */

    // Crear producto
    @PostMapping("/products")
    public ResponseEntity<ProductDTO> create(@Valid @RequestBody CreateProductRequest req) {
        ProductDTO created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + created.id()))
                .body(created);
    }

    // Reemplazar (PUT)
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id,
                                             @Valid @RequestBody UpdateProductRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    // (Opcional) PATCH: si lo implementas en el service
    // @PatchMapping("/products/{id}") ...

    // Eliminar
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Listar todos (para pruebas/admin)
    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    /** -------- existentes -------- */

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/products/{id}/decrement")
    public ResponseEntity<Void> decrement(@PathVariable Long id, @RequestParam @Min(1) int qty){
        service.decrement(id, qty);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<ProductDTO>> search(@RequestParam String q){
        searchLogService.logAsync(q);
        return ResponseEntity.ok(service.search(q));
    }
}
