package com.api_getway.product_service.feature.product;

import com.api_getway.product_service.domain.Product;
import com.api_getway.product_service.feature.filemetadata.FileMetaDataRepository;
import com.api_getway.product_service.feature.product.dto.ProductRequest;
import com.api_getway.product_service.feature.product.dto.ProductResponse;
import com.api_getway.product_service.feature.product.dto.ProductUpdateRequest;
import com.api_getway.product_service.feature.telegrambot.TelegramBotService;
import com.api_getway.product_service.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final FileMetaDataRepository fileMetaDataRepository;

    private final ProductMapper productMapper;

    private final TelegramBotService telegramBotService;

    @Override
    public void createProduct(ProductRequest productRequest) {

        if (productRequest.image() != null && !productRequest.image().trim().isEmpty() && !fileMetaDataRepository.existsByFileName(productRequest.image())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("imageName = %s has not been found, " +
                    "please upload first", productRequest.image()));
        }

        Product product = productMapper.fromRequest(productRequest);

        productRepository.save(product);

        telegramBotService.sendProductResponse(product);

    }

    @Override
    public Page<ProductResponse> getAllProducts(int pageNumber, int pageSize) {

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        Page<Product> productPage = productRepository.findAll(pageRequest);

        return productPage.map(productMapper::toResponse);
    }

    @Override
    public ProductResponse getProductById(String id) {

        Product product =
                productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("product = %s has not been found", id)));

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse updateProductById(String id, ProductUpdateRequest productUpdateRequest) {

        Product product =
                productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("product = %s has not been found", id)));

        productMapper.updateProductFromRequest(product, productUpdateRequest);

        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    public void deleteProductById(String id) {


        Product product =
                productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("product = %s has not been found", id)));

        productRepository.delete(product);

    }
}
