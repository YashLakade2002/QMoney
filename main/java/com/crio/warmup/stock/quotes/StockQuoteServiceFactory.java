
package com.crio.warmup.stock.quotes;

import org.springframework.web.client.RestTemplate;

public enum StockQuoteServiceFactory {

  INSTANCE;


  public StockQuotesService getService(String provider,  RestTemplate restTemplate) {

    if(provider == null) return new AlphavantageService(restTemplate);
    else if(provider.equalsIgnoreCase("tiingo")) return new TiingoService(restTemplate);
    else return new AlphavantageService(restTemplate);

  }
}
