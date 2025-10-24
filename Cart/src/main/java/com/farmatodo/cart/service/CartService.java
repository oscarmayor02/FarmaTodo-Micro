package com.farmatodo.cart.service;

import com.farmatodo.cart.domain.CartItem;
import com.farmatodo.cart.dto.CartItemDTO;
import com.farmatodo.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository repo;
    private final ProductClient productClient;

    @Value("${clients.product.validate:true}")
    private boolean validateProduct;

    @Transactional
    public CartItemDTO addItem(Long customerId, Long productId, int qty){
        if (qty <= 0) throw bad("qty must be > 0");

        String productName = null;

        // Trae el producto una sola vez (sirve para validar y para el nombre)
        var productOpt = productClient.get(productId);
        if (validateProduct && productOpt.isEmpty())
            throw bad("Product not found: " + productId);
        productName = productOpt.map(ProductSummary::name).orElse(null);

        var item = repo.findByCustomerIdAndProductId(customerId, productId)
                .map(ci -> { ci.setQty(ci.getQty() + qty); ci.setUpdatedAt(Instant.now()); return ci; })
                .orElse(CartItem.builder()
                        .customerId(customerId)
                        .productId(productId)
                        .qty(qty)
                        .updatedAt(Instant.now())
                        .build());

        var saved = repo.save(item);

        return new CartItemDTO(saved.getId(), saved.getCustomerId(), saved.getProductId(), saved.getQty(), productName);
    }

    @Transactional(readOnly = true)
    public List<CartItemDTO> get(Long customerId){
        return repo.findByCustomerId(customerId).stream()
                .map(i -> new CartItemDTO(i.getId(), i.getCustomerId(), i.getProductId(), i.getQty(), null)) // opcional: rellenar nombres con un batch si quieres
                .toList();
    }

    @Transactional
    public void clear(Long customerId){ repo.deleteByCustomerId(customerId); }

    private ErrorResponseException bad(String m){ var e=new ErrorResponseException(HttpStatus.BAD_REQUEST); e.setDetail(m); return e; }
}
