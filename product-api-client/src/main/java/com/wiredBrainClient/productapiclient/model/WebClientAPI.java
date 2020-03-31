package com.wiredBrainClient.productapiclient.model;

import org.reactivestreams.Processor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WebClientAPI {
    private WebClient webClient;

    public WebClientAPI() {
//        this.webClient=WebClient.create("http://localhost:8080/products");
        this.webClient=WebClient.builder()
                .baseUrl("http://localhost:8080/products")
                .build();
    }

    private Mono<ResponseEntity<Product>> postNewProduct(){
        return webClient.post()
                .body(Mono.just(new Product(null,"Pro4",4.99)),Product.class)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(Product.class))
                .doOnSuccess(productResponseEntity -> System.out.println("POST:::::::: "+productResponseEntity));
    }
    private Flux<Product> getallProducts(){
        return webClient.get()
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(product -> System.out.println("GET::::: "+product));
    }

    private  Mono<Product> updateProduct(String id,String name, Double price){
        return webClient.put()
                .uri("/{id}",id)
                .body(Mono.just(new Product(null, name, price)),Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(product -> System.out.println("UPDATE:::::: "+product));
    }

    private  Mono<Void> deleteProduct(String id){
        return webClient.delete()
                .uri("/{id}",id)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(product -> System.out.println("DELETE:::::: "+product));
    }

    private Flux<ProductEvent>  getAllEvents(){
        return webClient.get()
                .uri("/events")
                .retrieve()
                .bodyToFlux(ProductEvent.class);
    }


    public static void main(String[] args) {
        WebClientAPI webClientAPI =new WebClientAPI();
        webClientAPI.postNewProduct()
                .thenMany(webClientAPI.getallProducts())
                .take(1)
                .flatMap(product -> webClientAPI.updateProduct(product.getId(),"Pro5",5.99))
                .flatMap(product -> webClientAPI.deleteProduct(product.getId()))
                .thenMany(webClientAPI.getallProducts())
                .thenMany(webClientAPI.getAllEvents())
        .subscribe(System.out::println);
    }


}
