
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.dto.AlphavantageCandle;

import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {
  public static final String TOKEN = "LPXYX0SU8RCBFKNY";
  public static final String FUNCTION = "TIME_SERIES_DAILY";

  private RestTemplate restTemplate;

  protected AlphavantageService(RestTemplate restTemplate){
    this.restTemplate = restTemplate;
  }

  protected String buildUri(String symbol){
    String uriTemplate = String.format("https://www.alphavantage.co/query?function=%s&symbol=%s&apikey=%s", FUNCTION, symbol, TOKEN);
    return uriTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException, StockQuoteServiceException, RuntimeException{
    List<Candle> stocks = new ArrayList<>();
    Map<LocalDate, AlphavantageCandle> dailyResponses;
    try{
    String url = buildUri(symbol);
    String apiResponse = restTemplate.getForObject(url, String.class);

    ObjectMapper mapper = getObjectMapper();
 
    dailyResponses = mapper.readValue(apiResponse, AlphavantageDailyResponse.class).getCandles();

    if(dailyResponses == null || apiResponse == null)
      throw new StockQuoteServiceException("invalid");

      for(LocalDate date = from; !date.isAfter(to);  date = date.plusDays(1)){
        AlphavantageCandle candle = dailyResponses.get(date);
  
        if(candle != null){
          candle.setDate(date);
          stocks.add(candle);
        } 
      }
      Collections.sort(stocks, sortByDate);
    }
    catch(JsonProcessingException e){
      throw new StockQuoteServiceException("invalid");
    }
    catch(NullPointerException e){
      throw new StockQuoteServiceException("Alphantage returned invalid response", e);
    }
    return stocks;
  }

  public static final Comparator<Candle> sortByDate = new Comparator<Candle>() { 
    public int compare (Candle t1, Candle t2) { 
      return (int) (t1.getDate().compareTo(t2.getDate()));
    }
  };

}

