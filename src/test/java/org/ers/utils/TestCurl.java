package org.ers.utils;

import org.ers.model.LoginRequest;
import org.ers.model.LoginResponse;
import org.ers.model.ProductList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestCurl {

    String baseUrl = "https://dummyjson.com/";
    String productsEndpoint = baseUrl.concat("products");
    String authEndpoint = baseUrl.concat("auth/login");
    String username = "kminchelle"; // https://dummyjson.com/docs/auth
    String password = "0lelplR"; // https://dummyjson.com/docs/auth

    @Test
    void testDoGetWithCurl() throws IOException {

        ProductList productList = Curl.executor()
                .get(productsEndpoint)
                .header("Accept", "application/json")
                .execute(ProductList.class);

        assertThat(productList).isNotNull();
        assertThat(productList.getProducts().size()).isNotEqualTo(0);
        assertThat(productList.getLimit()).isEqualTo(30);
        assertThat(productList.getTotal()).isEqualTo(100);
    }

    @Test
    void testDoPostWithCurl() throws IOException {
        String addProductEndpoint = productsEndpoint.concat("/add");
        ProductList.Product productToAdd = prepareAddProduct();

        String response = Curl.executor()
                .post(addProductEndpoint)
                .header("Content-Type", "application/json")
                .pojoToJson(productToAdd)
                .enableLogs(true)
                .execute(String.class);

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo("{\"id\":101}");
    }

    @Test
    void testDoGetWithBearer() throws IOException {

        LoginResponse response = login();

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotNull();

        String token = response.getToken();

        String securedProductsAPI = baseUrl.concat("auth/products");
        ProductList securedProducts = Curl.executor()
                .get(securedProductsAPI)
                .header("Accept", "application/json")
                .bearerAuth(token)
                .enableLogs(true)
                .execute(ProductList.class);

        assertThat(securedProducts).isNotNull();
        assertThat(securedProducts.getProducts().size()).isNotEqualTo(0);
        assertThat(securedProducts.getLimit()).isEqualTo(30);
        assertThat(securedProducts.getTotal()).isEqualTo(100);
    }

    private LoginResponse login() throws IOException {
        return Curl.executor()
                .post(authEndpoint)
                .header("Accept", "application/json")
                .pojoToJson(LoginRequest.of(username, password))
                .execute(LoginResponse.class);
    }

    private ProductList.Product prepareAddProduct() {
        ProductList.Product product = new ProductList.Product();
        product.setTitle("iPhone 14");
        product.setDescription("An apple mobile which is nothing like apple");
        product.setPrice(549);
        product.setDiscountPercentage(12.96);
        product.setRating(4.69);
        product.setStock(94);
        product.setBrand("Apple");
        product.setCategory("smartphones");
        product.setThumbnail("https://i.dummyjson.com/data/products/1/thumbnail.jpg");
        List<String> images = new ArrayList<>();
        images.add("https://i.dummyjson.com/data/products/1/1.jpg");
        images.add("https://i.dummyjson.com/data/products/1/2.jpg");
        images.add("https://i.dummyjson.com/data/products/1/3.jpg");
        images.add("https://i.dummyjson.com/data/products/1/4.jpg");
        images.add("https://i.dummyjson.com/data/products/1/thumbnail.jpg");
        product.setImages(images);

        return product;
    }


}