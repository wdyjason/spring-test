package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RsEventDto;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RsEventRepository extends CrudRepository<RsEventDto, Integer> {
  List<RsEventDto> findAll();

  @Transactional
  void deleteAllByUserId(int userId);

  List<RsEventDto> findAllByOrderByVoteNumDesc();

  @Modifying
  @Query(value = "UPDATE RsEventDto r SET r.rank = ?1 WHERE r.id = ?2")
  void updateRank(int rank, int id);

  Optional<RsEventDto> findByRank(Integer rank);
}
