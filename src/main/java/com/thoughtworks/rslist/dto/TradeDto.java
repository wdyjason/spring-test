package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "trade_history")
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeDto {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer amount;

    private Integer rank;

    private Integer RsEventId;
}
