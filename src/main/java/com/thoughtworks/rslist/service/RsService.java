package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository,
                   VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public List<RsEvent> getEventListInOrder() {
    List<RsEventDto> dtoList = rsEventRepository.findAllByOrderByVoteNumDesc();
    List<RsEventDto> rankedList = dtoList
            .stream()
            .filter(f -> f.getRank() > 0)
            .sorted(Comparator.comparing(RsEventDto::getRank))
            .collect(Collectors.toList());

    dtoList.removeAll(rankedList);
    for (RsEventDto e : rankedList) {
        dtoList.add(e.getRank() - 1, e);
    }

    return dtoList.stream().map(item ->
            RsEvent.builder()
                    .eventName(item.getEventName())
                    .keyword(item.getKeyword())
                    .userId(item.getId())
                    .voteNum(item.getVoteNum())
                    .build())
            .collect(Collectors.toList());
  }

  public HttpStatus buy(Trade trade, int id) {
    Optional<RsEventDto> rsEventDtoWarped = rsEventRepository.findById(id);
    List<TradeDto> tradeDtoList =tradeRepository.findByRsEventIdOrderByAmountDesc(id);

    if (rsEventDtoWarped.isPresent() && tradeDtoList.size() > 0) {

      if (trade.getAmount() > tradeDtoList.get(0).getAmount() || rsEventDtoWarped.get().getRank() < 1)  {
        rsEventRepository.updateRank(trade.getRank(), id);
        tradeRepository.save(TradeDto.builder()
                .rank(trade.getRank())
                .amount(trade.getAmount())
                .RsEventId(id).build());
        return HttpStatus.OK;
      }

    }
    return HttpStatus.BAD_REQUEST;
  }
}
