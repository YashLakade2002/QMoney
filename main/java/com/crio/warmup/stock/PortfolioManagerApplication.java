
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
  
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    List<String> listOfSymbols = new ArrayList<String>();
    ObjectMapper objectMapper = getObjectMapper();
    File file = resolveFileFromResources(args[0]);
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    for (PortfolioTrade trade : trades) {
      listOfSymbols.add(trade.getSymbol());
    }
    return listOfSymbols;
  }


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {
     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/yashlakade2002-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@7c6908d7";
     String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicatinTest.mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "29:1";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

  public static List<TotalReturnsDto> mainReadQuotesHelper(String[] args, List<PortfolioTrade> trades) throws IOException, URISyntaxException {
    RestTemplate restTemplate= new RestTemplate();
    List<TotalReturnsDto> tests = new ArrayList<TotalReturnsDto>(); 
    LocalDate endDate = LocalDate.parse(args[1]);
    for (PortfolioTrade t: trades) { 
      String uri = prepareUrl(t,endDate , getToken());
      TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class); 
      if (results != null) {
        tests.add(new TotalReturnsDto(t.getSymbol(), results [results.length - 1].getClose())); 
      }  
    }    
    return tests;    
    }

    public static final Comparator<TotalReturnsDto> closingComparator = new Comparator<TotalReturnsDto>() { 
        public int compare (TotalReturnsDto t1, TotalReturnsDto t2) { 
          return (int) (t1.getClosingPrice().compareTo(t2.getClosingPrice()));
      }
    };

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    List<TotalReturnsDto> sortedByValue = mainReadQuotesHelper(args, trades);
    Collections.sort(sortedByValue, closingComparator);
    List<String>stocks = new ArrayList<String>();
    for(TotalReturnsDto trd : sortedByValue){
      stocks.add(trd.getSymbol());
    }
    return stocks;
  }
  
  private static String readFileAsString(String fileName) throws IOException, URISyntaxException {
    return new String(Files.readAllBytes(resolveFileFromResources(fileName).toPath()), "UTF-8");
  }


  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    List<PortfolioTrade> listOfSymbols = new ArrayList<>();
    ObjectMapper objectMapper = getObjectMapper();
    File file = resolveFileFromResources(filename);
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    for (PortfolioTrade trade : trades) {
      listOfSymbols.add(trade);

    }
    return listOfSymbols;
  }


  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
    + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;

  }

  public static String getToken(){
    return "45d6d0acfafce47fae96aeecf340f9e035e47920";
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    if(!candles.isEmpty()) {
      return candles.get(0).getOpen();
    }
     return 0.0;
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    if(!candles.isEmpty()){
      return candles.get(candles.size()-1).getClose();
    }
    return 0.0;
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate= new RestTemplate();
    String url = prepareUrl(trade, endDate, token);
    TiingoCandle[] results = restTemplate.getForObject(url, TiingoCandle[].class); 
    List<Candle> res = new ArrayList<>();
    if(results != null){
      for(TiingoCandle result : results){
        res.add(result);
      }
    }
    return res;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
        List<PortfolioTrade> Trades = readTradesFromJson(args[0]);
        List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
        for(PortfolioTrade trade : Trades){
          LocalDate endDate = LocalDate.parse(args[1]);
          List<Candle> candles = fetchCandles(trade, endDate, getToken());
          Double openingPrice = getOpeningPriceOnStartDate(candles);
          Double clossingPrice = getClosingPriceOnEndDate(candles);
          annualizedReturns.add(calculateAnnualizedReturns(endDate, trade, openingPrice, clossingPrice));
        }
        Collections.sort(annualizedReturns, new Comparator<AnnualizedReturn>() {
          @Override
          public int compare(AnnualizedReturn arg0, AnnualizedReturn arg1) {            
            return (int) arg0.getAnnualizedReturn().compareTo(arg1.getAnnualizedReturn());
          }
          
        });
        Collections.reverse(annualizedReturns);        
     return annualizedReturns;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double AbsoluteReturn = (sellPrice - buyPrice) / buyPrice;
      String symbol = trade.getSymbol();
      LocalDate purchasedDate = trade.getPurchaseDate(); 
      Double noOfYears = (double) ChronoUnit.DAYS.between( purchasedDate , endDate ) / 365;
      Double annualizedReturn = Math.pow((1 + AbsoluteReturn), (1 / noOfYears))-1;
      return new AnnualizedReturn(symbol, annualizedReturn, AbsoluteReturn);
  }


  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

