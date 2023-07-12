
package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;



public class PortfolioManagerImpl implements PortfolioManager {
  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;
  
  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  
  public PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) throws StockQuoteServiceException{
    AnnualizedReturn annualizedReturn;
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
  
    for(int i=0; i<portfolioTrades.size();i++){   
      annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i), endDate);
      annualizedReturns.add(annualizedReturn);
    }

    Comparator<AnnualizedReturn> sortByAnnReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    Collections.sort(annualizedReturns, sortByAnnReturn);
    return annualizedReturns;
  }

  
  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) throws StockQuoteServiceException{
    String symbol = trade.getSymbol();    
    LocalDate startDate = trade.getPurchaseDate();
    Double buyPrice = 0.0, sellPrice = 0.0;

    try {
      LocalDate startLocalDate = trade.getPurchaseDate(); 
      List<Candle> stocksStartToEndFull = getStockQuote(symbol, startLocalDate, endDate);
      Collections.sort(stocksStartToEndFull, (candle1, candle2) -> {
        return candle1.getDate().compareTo(candle2.getDate());
      });
      Candle stockStartDate = stocksStartToEndFull.get(0);
      Candle stocksLatest = stocksStartToEndFull.get(stocksStartToEndFull.size()-1);

      buyPrice = stockStartDate.getOpen();
      sellPrice = stocksLatest.getClose();
      endDate = stocksLatest.getDate();
    }catch(JsonProcessingException e){
      throw new RuntimeException();
    }

    Double totalReturn = (sellPrice - buyPrice) / buyPrice;
      
    Double noOfYears = (double) ChronoUnit.DAYS.between( startDate , endDate ) / 365;
    Double annualizedReturns = Math.pow((1 + totalReturn), (1 / noOfYears))-1;
    return new AnnualizedReturn(symbol, annualizedReturns, totalReturn);
  } 


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException, StockQuoteServiceException {
     return stockQuotesService.getStockQuote(symbol, from, to);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    return "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate + "&endDate=" + endDate + "&token=" + getToken();
  }

  private String getToken(){
    return "45d6d0acfafce47fae96aeecf340f9e035e47920";
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }


  @Override
public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads) throws InterruptedException, StockQuoteServiceException {
  List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
  List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();
  final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

  for (int i = 0; i < portfolioTrades.size(); i++) {
    PortfolioTrade trade = portfolioTrades.get(i);
    Callable<AnnualizedReturn> callableTask = () -> {
      return getAnnualizedReturn(trade, endDate);
    };
    Future<AnnualizedReturn> futureReturns = pool.submit(callableTask);
    futureReturnsList.add(futureReturns);
  }

  for (int i = 0; i < portfolioTrades.size(); i++) {
    Future<AnnualizedReturn> futureReturns = futureReturnsList.get(i);
    try {
      AnnualizedReturn returns = futureReturns.get();
      annualizedReturns.add(returns);
    } catch (ExecutionException e) {
      throw new StockQuoteServiceException("Error when calling the API", e);

    }
  }
  Collections.sort(annualizedReturns, getComparator());
  return annualizedReturns;
}



}
