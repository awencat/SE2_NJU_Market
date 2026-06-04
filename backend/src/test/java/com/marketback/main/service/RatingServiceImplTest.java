package com.marketback.main.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.marketback.main.dto.RatingSummary;
import com.marketback.main.entity.OrderEntity;
import com.marketback.main.entity.Rating;
import com.marketback.main.mapper.RatingMapper;
import com.marketback.main.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private OrderEntityService orderEntityService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private RatingMapper ratingMapper;

    private RatingServiceImpl ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingServiceImpl(orderEntityService, orderItemService);
        ReflectionTestUtils.setField(ratingService, "baseMapper", ratingMapper);
    }

    @Test
    void saveAcceptsPurchasedGoodAndPersistsRating() {
        when(orderEntityService.list(any(Wrapper.class))).thenReturn(List.of(order(30, 1)));
        when(orderItemService.count(any())).thenReturn(1L);
        when(ratingMapper.selectCount(any())).thenReturn(0L);
        when(ratingMapper.insert(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setRatingId(200);
            return 1;
        });

        Rating rating = rating(1, 10, 5);
        boolean saved = ratingService.save(rating);

        assertThat(saved).isTrue();
        assertThat(rating.getRatingId()).isEqualTo(200);
        verify(ratingMapper).insert(rating);
    }

    @Test
    void saveRejectsScoreOutsideOneToFive() {
        assertThatThrownBy(() -> ratingService.save(rating(1, 10, 6)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("score must be between 1 and 5");

        verify(orderEntityService, never()).list(any(Wrapper.class));
        verify(ratingMapper, never()).insert(any(Rating.class));
    }

    @Test
    void saveRejectsUserWhoDidNotBuyGood() {
        when(orderEntityService.list(any(Wrapper.class))).thenReturn(List.of(order(30, 1)));
        when(orderItemService.count(any())).thenReturn(0L);

        assertThatThrownBy(() -> ratingService.save(rating(1, 10, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("only buyer can rate this good");

        verify(ratingMapper, never()).insert(any(Rating.class));
    }

    @Test
    void saveRejectsDuplicateRating() {
        when(orderEntityService.list(any(Wrapper.class))).thenReturn(List.of(order(30, 1)));
        when(orderItemService.count(any())).thenReturn(1L);
        when(ratingMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> ratingService.save(rating(1, 10, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user already rated this good");

        verify(ratingMapper, never()).insert(any(Rating.class));
    }

    @Test
    void summaryByGoodRoundsAverageToOneDecimalPlace() {
        when(ratingMapper.selectList(any())).thenReturn(List.of(
                rating(1, 10, 5),
                rating(2, 10, 4),
                rating(3, 10, 4)
        ));

        RatingSummary summary = ratingService.summaryByGood(10);

        assertThat(summary.getGoodId()).isEqualTo(10);
        assertThat(summary.getAverageScore()).isEqualTo(4.3);
        assertThat(summary.getRatingCount()).isEqualTo(3);
    }

    private Rating rating(Integer userId, Integer goodId, Integer score) {
        Rating rating = new Rating();
        rating.setUserId(userId);
        rating.setGoodId(goodId);
        rating.setScore(score);
        return rating;
    }

    private OrderEntity order(Integer orderId, Integer buyerId) {
        OrderEntity order = new OrderEntity();
        order.setOrderId(orderId);
        order.setBuyerId(buyerId);
        return order;
    }
}
