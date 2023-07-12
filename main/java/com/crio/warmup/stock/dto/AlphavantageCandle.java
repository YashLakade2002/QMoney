package com.crio.warmup.stock.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphavantageCandle implements Candle {
  @JsonProperty("1. open")
  private Double open;

  @JsonProperty("2. high")
  private Double high;

  @JsonProperty("3. low")
  private Double low;

  @JsonProperty("4. close")
  private Double close;

  private LocalDate date;

  public void setHigh(Double high) {
    this.high = high;
  }

  public void setLow(Double low) {
    this.low = low;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Double getClose(){
    return close;
  }

  public void setClose(Double close){
    this.close = close;
  }

  public Double getOpen(){
    return open;
  }

  public void setOpen(Double open){
    this.open = open;
  }

  public Double getHigh() {
    return high;
  }

  
  public Double getLow() {
    return low;
  }

  
  public LocalDate getDate() {
    return date;
  }

}

