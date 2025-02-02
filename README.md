For retrieveing historical stock data, Yahoo Finance API did not work. So we decided to use Alpha Vantage which has free API Key. Alpha Vantage: API results usually look like JSON with timestamps, open, high, low, close prices, and volume. <br>
cd src/main/java/com/stockanalysis <br>
6HY5V13LX2LO0MKO <br>
go to project root: cd ~/Desktop/SDM/StockAnalysis <br>
mvn exec:java -Dexec.mainClass="com.stockanalysis.StockDataFetcher" <br>

