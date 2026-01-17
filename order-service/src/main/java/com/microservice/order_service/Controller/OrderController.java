package com.microservice.order_service.Controller;

import com.microservice.order_service.DTO.OrderResponseDTO;
import com.microservice.order_service.DTO.ProductDTO;
import com.microservice.order_service.Entity.Order;
import com.microservice.order_service.Repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.List;

@RestController
@RequestMapping("orders")

public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WebClient.Builder webClientConfig;

    private final String PRODUCT_SERVICE = "product-service";

    @PostMapping("/orderPlacing")
    @CircuitBreaker(name=PRODUCT_SERVICE,fallbackMethod = "displayErrorPage")
    @RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "fallbackMethod")
    public Mono<ResponseEntity<OrderResponseDTO>> placeOrder(@RequestBody Order order){
        return webClientConfig.build().get().uri("http://localhost:8081/products/"+order.getProductId())
                .retrieve().bodyToMono(ProductDTO.class).map(productDTO -> {
                    OrderResponseDTO orderResponseDTO = new OrderResponseDTO();

                    orderResponseDTO.setProductId(order.getProductId());
                    orderResponseDTO.setQuantity(order.getQuantity());

                    orderResponseDTO.setProductName(productDTO.getProductName());
                    orderResponseDTO.setProductPrice(productDTO.getProductPrice() * order.getQuantity());
                    orderResponseDTO.setPrice(productDTO.getProductPrice());
                    orderRepository.save(order);
                    orderResponseDTO.setOrderId(order.getOrderId());
                    return ResponseEntity.ok(orderResponseDTO);
                });
    }

    @GetMapping
    public List<Order> getOrders(){
        return orderRepository.findAll();
    }

    public Mono<ResponseEntity<OrderResponseDTO>> displayErrorPage(Order order, Throwable throwable)
    {
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setProductId(100L);
        orderResponseDTO.setQuantity(100);
        orderResponseDTO.setProductName("Error");
        orderResponseDTO.setProductPrice(0.0);
        orderResponseDTO.setOrderId(0L);

        // Optional: log the cause of failure
            System.err.println("Fallback triggered due to: " + throwable.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(orderResponseDTO));
    }

    public Mono<ResponseEntity<OrderResponseDTO>> fallbackMethod(
            Order order,
            RequestNotPermitted ex
    ) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setProductId(order.getProductId());
        dto.setQuantity(order.getQuantity());
        dto.setProductName("Rate limit exceeded");
        dto.setProductPrice(0.0);
        dto.setOrderId(0L);

        System.err.println("RateLimiter triggered: " + ex.getMessage());

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(dto)
        );
    }



}
