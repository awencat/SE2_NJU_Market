package com.marketback.main.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.marketback.main.dto.PurchaseRequest;
import com.marketback.main.dto.PurchaseResponse;
import com.marketback.main.entity.Good;
import com.marketback.main.entity.OrderEntity;
import com.marketback.main.entity.OrderItem;
import com.marketback.main.entity.User;
import com.marketback.main.mapper.OrderEntityMapper;
import com.marketback.main.service.impl.OrderEntityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEntityServiceImplTest {

    @Mock
    private GoodService goodService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private UserService userService;

    @Mock
    private OrderEntityMapper orderEntityMapper;

    private OrderEntityServiceImpl orderEntityService;

    @BeforeEach
    void setUp() {
        orderEntityService = new OrderEntityServiceImpl(goodService, orderItemService, userService);
        ReflectionTestUtils.setField(orderEntityService, "baseMapper", orderEntityMapper);
    }

    @Test
    void purchaseCreatesOrderItemAndDeductsStock() {
        User buyer = user(1, "buyer");
        User seller = user(2, "seller");
        seller.setPhone("13800000000");
        seller.setEmail("seller@njumarket.test");
        Good good = good(10, 2, 5, "Java Book", "19.90");

        when(userService.getById(1)).thenReturn(buyer);
        when(userService.getById(2)).thenReturn(seller);
        when(goodService.getById(10)).thenReturn(good);
        when(orderEntityMapper.insert(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setOrderId(100);
            return 1;
        });
        when(orderItemService.save(any(OrderItem.class))).thenReturn(true);
        when(goodService.updateById(any(Good.class))).thenReturn(true);

        PurchaseResponse response = orderEntityService.purchase(purchaseRequest(1, 10, 2));

        assertThat(response.getOrderId()).isEqualTo(100);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getGoodTitle()).isEqualTo("Java Book");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("39.80");
        assertThat(response.getSellerName()).isEqualTo("seller");

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemService).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getOrderId()).isEqualTo(100);
        assertThat(itemCaptor.getValue().getQuantity()).isEqualTo(2);
        assertThat(itemCaptor.getValue().getSubtotal()).isEqualByComparingTo("39.80");

        ArgumentCaptor<Good> goodCaptor = ArgumentCaptor.forClass(Good.class);
        verify(goodService).updateById(goodCaptor.capture());
        assertThat(goodCaptor.getValue().getCount()).isEqualTo(3);
    }

    @Test
    void purchaseRejectsInvalidCountBeforeChangingData() {
        assertThatThrownBy(() -> orderEntityService.purchase(purchaseRequest(1, 10, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("count must be at least 1");

        verify(orderEntityMapper, never()).insert(any(OrderEntity.class));
        verify(goodService, never()).updateById(any(Good.class));
        verify(orderItemService, never()).save(any(OrderItem.class));
    }

    @Test
    void purchaseRejectsOwnGood() {
        when(userService.getById(1)).thenReturn(user(1, "buyer"));
        when(goodService.getById(10)).thenReturn(good(10, 1, 5, "Own Book", "10.00"));

        assertThatThrownBy(() -> orderEntityService.purchase(purchaseRequest(1, 10, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("buyer cannot purchase own good");

        verify(orderEntityMapper, never()).insert(any(OrderEntity.class));
        verify(orderItemService, never()).save(any(OrderItem.class));
    }

    @Test
    void purchaseRejectsCountGreaterThanStock() {
        when(userService.getById(1)).thenReturn(user(1, "buyer"));
        when(goodService.getById(10)).thenReturn(good(10, 2, 1, "Notebook", "8.00"));

        assertThatThrownBy(() -> orderEntityService.purchase(purchaseRequest(1, 10, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("purchase count exceeds remaining stock");

        verify(orderEntityMapper, never()).insert(any(OrderEntity.class));
        verify(goodService, never()).updateById(any(Good.class));
    }

    @Test
    void cancelRestoresStockRemovesItemsAndOrder() {
        OrderEntity order = new OrderEntity();
        order.setOrderId(20);
        order.setBuyerId(1);
        order.setSellerId(2);

        OrderItem item = new OrderItem();
        item.setOrderId(20);
        item.setGoodId(10);
        item.setQuantity(2);

        Good good = good(10, 2, 3, "Keyboard", "30.00");

        when(orderEntityMapper.selectById(20)).thenReturn(order);
        when(orderItemService.list(any(Wrapper.class))).thenReturn(List.of(item));
        when(goodService.getById(10)).thenReturn(good);
        when(goodService.updateById(any(Good.class))).thenReturn(true);
        when(orderItemService.remove(any())).thenReturn(true);
        when(orderEntityMapper.deleteById(20)).thenReturn(1);

        OrderEntity cancelled = orderEntityService.cancel(20, 1);

        assertThat(cancelled).isSameAs(order);
        ArgumentCaptor<Good> goodCaptor = ArgumentCaptor.forClass(Good.class);
        verify(goodService).updateById(goodCaptor.capture());
        assertThat(goodCaptor.getValue().getCount()).isEqualTo(5);
        verify(orderItemService).remove(any());
        verify(orderEntityMapper).deleteById(20);
    }

    @Test
    void cancelRejectsNonBuyer() {
        OrderEntity order = new OrderEntity();
        order.setOrderId(20);
        order.setBuyerId(1);
        when(orderEntityMapper.selectById(20)).thenReturn(order);

        assertThatThrownBy(() -> orderEntityService.cancel(20, 99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("only buyer can cancel this order");

        verify(orderItemService, never()).list(any(Wrapper.class));
        verify(orderEntityMapper, never()).deleteById(any());
    }

    private PurchaseRequest purchaseRequest(Integer buyerId, Integer goodId, Integer count) {
        PurchaseRequest request = new PurchaseRequest();
        request.setBuyerId(buyerId);
        request.setGoodId(goodId);
        request.setCount(count);
        return request;
    }

    private User user(Integer userId, String username) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(username + "@njumarket.test");
        return user;
    }

    private Good good(Integer goodId, Integer sellerId, Integer count, String title, String price) {
        Good good = new Good();
        good.setGoodId(goodId);
        good.setSellerId(sellerId);
        good.setCount(count);
        good.setTitle(title);
        good.setPrice(new BigDecimal(price));
        return good;
    }
}
