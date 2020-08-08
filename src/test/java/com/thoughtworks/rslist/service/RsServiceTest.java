package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

//@TestMethodOrder(value = )
class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;
  LocalDateTime localDateTime;
  Vote vote;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }

  @Test
  void shouldBuyAnEventSuccessWhenItIsFree() {
    Trade trade = Trade.builder()
            .rank(1)
            .amount(100)
            .build();
    RsEventDto rsEventDto = RsEventDto.builder()
            .eventName("event")
            .id(3)
            .rank(0)
            .voteNum(0)
            .keyword("key")
            .build();
    TradeDto toFound = TradeDto.builder()
            .id(1)
            .amount(50)
            .rank(2)
            .rsEventId(3)
            .build();
    TradeDto toSaved = TradeDto.builder()
            .amount(100)
            .rank(1)
            .rsEventId(3)
            .build();

    when(rsEventRepository.findById(3)).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findByRsEventIdOrderByAmountDesc(3)).thenReturn(Arrays.asList(toFound));
    when(rsEventRepository.findByRank(1)).thenReturn(Optional.empty());
    rsService.buy(trade, 3);

    verify(rsEventRepository).updateRank(1, 3);
    verify(tradeRepository).save(toSaved);
  }

  @Test
  void shouldBuyAnEventSuccessWhenItIsPayHigherAmount() {
    Trade trade = Trade.builder()
            .rank(1)
            .amount(100)
            .build();
    RsEventDto rsEventDto = RsEventDto.builder()
            .eventName("event")
            .id(3)
            .rank(2)
            .voteNum(0)
            .keyword("key")
            .build();
    RsEventDto sameRankEvent = RsEventDto.builder()
            .eventName("event")
            .id(2)
            .rank(1)
            .voteNum(0)
            .keyword("sameRank")
            .build();
    TradeDto toFound = TradeDto.builder()
            .id(1)
            .amount(50)
            .rank(2)
            .rsEventId(3)
            .build();
    TradeDto toSaved = TradeDto.builder()
            .amount(100)
            .rank(1)
            .rsEventId(3)
            .build();

    when(rsEventRepository.findById(3)).thenReturn(Optional.of(rsEventDto));
    when(rsEventRepository.findByRank(1)).thenReturn(Optional.of(sameRankEvent));
    when(tradeRepository.findByRsEventIdOrderByAmountDesc(3)).thenReturn(Arrays.asList(toFound));
    rsService.buy(trade, 3);

    verify(rsEventRepository, times(2)).updateRank(anyInt(), anyInt());
    verify(tradeRepository).save(toSaved);
  }

  @Test
//  @Order(1)
  void shouldBuyAnEventFailWhenItIsPayNotEnough() {
    Trade trade = Trade.builder()
            .rank(1)
            .amount(100)
            .build();
    RsEventDto rsEventDto = RsEventDto.builder()
            .eventName("event")
            .id(3)
            .rank(2)
            .voteNum(0)
            .keyword("key")
            .build();
    TradeDto toFound = TradeDto.builder()
            .id(1)
            .amount(200)
            .rank(2)
            .rsEventId(3)
            .build();
    TradeDto toSaved = TradeDto.builder()
            .amount(100)
            .rank(1)
            .rsEventId(3)
            .build();

    when(rsEventRepository.findById(3)).thenReturn(Optional.of(rsEventDto));
    when(rsEventRepository.findByRank(1)).thenReturn(Optional.empty());
    when(tradeRepository.findByRsEventIdOrderByAmountDesc(3)).thenReturn(Arrays.asList(toFound));

    assertThrows(RequestNotValidException.class, () ->  rsService.buy(trade, 3));
  }
}
