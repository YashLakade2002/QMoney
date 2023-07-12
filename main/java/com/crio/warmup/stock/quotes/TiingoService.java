
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {
  public static final String TOKEN = "9299cc3a9124368f351d0e5e04e01e39641f1a6b";
  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)  throws JsonProcessingException, StockQuoteServiceException {
    if(from.compareTo(to) >= 0){
      throw new RuntimeException();
    }
    TiingoCandle[] res;
    try{
      String url = buildUri(symbol, from, to);
      String apiResponse = restTemplate.getForObject(url, String.class);
      ObjectMapper mapper = getObjectMapper();
      res = mapper.readValue(apiResponse, TiingoCandle[].class);

      if(res == null){
        throw new StockQuoteServiceException("invalid");
      } 
    } catch(JsonProcessingException e) {
        throw new StockQuoteServiceException("invalid");
    } catch(NullPointerException e){
      throw new StockQuoteServiceException("Error occured when requesting response from Tiingo API", e.getCause());
    }
      List<Candle> stocks = Arrays.asList(res);  
      Collections.sort(stocks, sortByDate); 
      return stocks;
  }

  public static final Comparator<Candle> sortByDate = new Comparator<Candle>() { 
    public int compare (Candle t1, Candle t2) { 
      return (int) (t1.getDate().compareTo(t2.getDate()));
    }
  };

  public String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    return "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate + "&endDate=" + endDate + "&token=" + getToken();
  }

  private String getToken(){
    return "9299cc3a9124368f351d0e5e04e01e39641f1a6b";
  }

};
