package com.marketback.main.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketback.main.config.AvatarStorageProperties;
import com.marketback.main.config.GoodsImageStorageProperties;
import com.marketback.main.dto.AuthResponse;
import com.marketback.main.dto.LoginRequest;
import com.marketback.main.dto.PurchaseRequest;
import com.marketback.main.dto.PurchaseResponse;
import com.marketback.main.dto.RegisterRequest;
import com.marketback.main.entity.Good;
import com.marketback.main.entity.Rating;
import com.marketback.main.service.GoodImageService;
import com.marketback.main.service.GoodService;
import com.marketback.main.service.OrderEntityService;
import com.marketback.main.service.RatingService;
import com.marketback.main.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        GoodController.class,
        OrderEntityController.class,
        RatingController.class
})
@Import({
        com.marketback.main.exception.GlobalExceptionHandler.class,
        CoreBusinessFlowIntegrationTest.StorageTestConfig.class
})
class CoreBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private GoodService goodService;

    @MockBean
    private GoodImageService goodImageService;

    @MockBean
    private OrderEntityService orderEntityService;

    @MockBean
    private RatingService ratingService;

    private final AtomicInteger userIds = new AtomicInteger(1);
    private final AtomicInteger goodIds = new AtomicInteger(10);
    private final AtomicInteger orderIds = new AtomicInteger(100);
    private final AtomicInteger ratingIds = new AtomicInteger(200);
    private final Map<String, Integer> usersByName = new HashMap<>();
    private final Map<String, String> passwordsByName = new HashMap<>();
    private final Map<Integer, Good> goods = new HashMap<>();
    private final Set<String> purchasedGoodKeys = new HashSet<>();
    private final Set<String> ratingKeys = new HashSet<>();

    @BeforeEach
    void setUp() {
        usersByName.clear();
        passwordsByName.clear();
        goods.clear();
        purchasedGoodKeys.clear();
        ratingKeys.clear();

        when(userService.register(any(RegisterRequest.class))).thenAnswer(invocation -> {
            RegisterRequest request = invocation.getArgument(0);
            if (usersByName.containsKey(request.getUsername())) {
                throw new IllegalArgumentException("username or email already exists");
            }
            int userId = userIds.getAndIncrement();
            usersByName.put(request.getUsername(), userId);
            passwordsByName.put(request.getUsername(), request.getPassword());
            return new AuthResponse(userId, request.getUsername(), request.getEmail(), request.getCampus(), null);
        });

        when(userService.login(any(LoginRequest.class))).thenAnswer(invocation -> {
            LoginRequest request = invocation.getArgument(0);
            Integer userId = usersByName.get(request.getUsername());
            if (userId == null || !request.getPassword().equals(passwordsByName.get(request.getUsername()))) {
                throw new IllegalArgumentException("username or password is incorrect");
            }
            return new AuthResponse(userId, request.getUsername(), request.getUsername() + "@njumarket.test", null, null);
        });

        when(goodService.save(any(Good.class))).thenAnswer(invocation -> {
            Good good = invocation.getArgument(0);
            good.setGoodId(goodIds.getAndIncrement());
            goods.put(good.getGoodId(), good);
            return true;
        });

        when(orderEntityService.purchase(any(PurchaseRequest.class))).thenAnswer(invocation -> {
            PurchaseRequest request = invocation.getArgument(0);
            Good good = goods.get(request.getGoodId());
            if (good == null) {
                throw new IllegalArgumentException("good not found");
            }
            if (request.getBuyerId().equals(good.getSellerId())) {
                throw new IllegalArgumentException("buyer cannot purchase own good");
            }
            if (request.getCount() > good.getCount()) {
                throw new IllegalArgumentException("purchase count exceeds remaining stock");
            }
            int remaining = good.getCount() - request.getCount();
            good.setCount(remaining);
            purchasedGoodKeys.add(request.getBuyerId() + ":" + request.getGoodId());
            BigDecimal totalAmount = good.getPrice().multiply(BigDecimal.valueOf(request.getCount()));
            int orderId = orderIds.getAndIncrement();
            return new PurchaseResponse(
                    orderId,
                    "ORD-TEST-" + orderId,
                    "PENDING",
                    good.getGoodId(),
                    good.getTitle(),
                    request.getCount(),
                    totalAmount,
                    good.getSellerId(),
                    "seller",
                    null,
                    "seller@njumarket.test"
            );
        });

        when(ratingService.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            if (rating.getScore() == null || rating.getScore() < 1 || rating.getScore() > 5) {
                throw new IllegalArgumentException("score must be between 1 and 5");
            }
            String key = rating.getUserId() + ":" + rating.getGoodId();
            if (!purchasedGoodKeys.contains(key)) {
                throw new IllegalArgumentException("only buyer can rate this good");
            }
            if (!ratingKeys.add(key)) {
                throw new IllegalArgumentException("user already rated this good");
            }
            rating.setRatingId(ratingIds.getAndIncrement());
            return true;
        });

        when(goodImageService.listByGoodId(any())).thenReturn(java.util.List.of());
        when(ratingService.list(any(Wrapper.class))).thenReturn(java.util.List.of());
    }

    @Test
    void completeHappyPathRegisterLoginPublishPurchaseAndRate() throws Exception {
        int sellerId = register("seller", "seller@njumarket.test");
        int buyerId = register("buyer", "buyer@njumarket.test");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"buyer","password":"secret1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("logged in"))
                .andExpect(jsonPath("$.data.userId").value(buyerId));

        int goodId = publishGood(sellerId);

        mockMvc.perform(post("/api/orders/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"buyerId":%d,"goodId":%d,"count":2}
                                """.formatted(buyerId, goodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("purchased"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(159.80));

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"goodId":%d,"score":5}
                                """.formatted(buyerId, goodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data.ratingId").value(200));
    }

    @Test
    void purchaseRejectsInvalidCount() throws Exception {
        int sellerId = register("seller", "seller@njumarket.test");
        int goodId = publishGood(sellerId);

        mockMvc.perform(post("/api/orders/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"buyerId":99,"goodId":%d,"count":0}
                                """.formatted(goodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("count must be at least 1"));
    }

    @Test
    void duplicateRatingIsRejectedAfterFirstSuccessfulRating() throws Exception {
        int sellerId = register("seller", "seller@njumarket.test");
        int buyerId = register("buyer", "buyer@njumarket.test");
        int goodId = publishGood(sellerId);

        mockMvc.perform(post("/api/orders/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"buyerId":%d,"goodId":%d,"count":1}
                                """.formatted(buyerId, goodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"goodId":%d,"score":4}
                                """.formatted(buyerId, goodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"goodId":%d,"score":5}
                                """.formatted(buyerId, goodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("user already rated this good"));
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        register("buyer", "buyer@njumarket.test");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"buyer","password":"wrong-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("username or password is incorrect")));
    }

    private int register(String username, String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"secret1","email":"%s","phone":"","campus":"仙林","address":"宿舍区"}
                                """.formatted(username, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.at("/data/userId").asInt();
    }

    private int publishGood(int sellerId) throws Exception {
        String response = mockMvc.perform(post("/api/goods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sellerId":%d,"title":"计算机网络教材","description":"九成新","price":79.90,"category":"BOOK","count":3,"condition":"USED"}
                                """.formatted(sellerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.at("/data/goodId").asInt();
    }

    @TestConfiguration
    static class StorageTestConfig {

        @Bean
        AvatarStorageProperties avatarStorageProperties() {
            return new AvatarStorageProperties();
        }

        @Bean
        GoodsImageStorageProperties goodsImageStorageProperties() {
            return new GoodsImageStorageProperties();
        }
    }
}
