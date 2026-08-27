package com.example.crypto_trading.response.Klines;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlineResponse {
  @JsonAlias("s")
  private String symbol;
  
  @JsonAlias("t")
  private Long startTime;
  
  @JsonAlias("T")
  private Long closeTime;
  
  @JsonAlias("i")
  private String interval;
  
  @JsonAlias("f")
  private Long firstTradeId;
  
  @JsonAlias("L")
  private Long lastTradeId;
  
  @JsonAlias("o")
  private String openPrice;
  
  @JsonAlias("c")
  private String closePrice;
  
  @JsonAlias("h")
  private String highPrice;
  
  @JsonAlias("l")
  private String lowPrice;
  
  @JsonAlias("v")
  private String volume;
  
  @JsonAlias("n")
  private Integer numberOfTrades;
  
  @JsonAlias("x")
  private Boolean isKlineClosed;
  
  @JsonAlias("q")
  private String quoteVolume;
  
  @JsonAlias("V")
  private String takerBuyBaseVolume;
  
  @JsonAlias("Q")
  private String takerBuyQuoteVolume;
}
