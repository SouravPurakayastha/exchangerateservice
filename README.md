Service: ExchangeRateService
-
Description:
1. Get daily and historical ECB published currency rates between any pair of ECB published currencies
2. Get list of supported currencies by ecb
3. Convert currencies from one to another at latest or any past day rate
4. Get statistics on count of the currencies searched for
5. Get URL to look up historical graph containing rates vs time data
Source of information: https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html

How to run the service?
- 
Prerequisites:
1. Docker
2. Java
3. Maven

Steps
1. Download the zip from the file sharing portal
2. Unzip and navigate to the root folder of the project in the terminal
   1. run `mvn clean install` (assuming you have Java and Maven)
   2. Please note that the service is developed with OpenJdk 18.x version
3. Check if you have docker running in your system. Check if the ports mentioned in the `docker-compose.yml` are free 
4. Run `docker-compose up`
5. If the above steps does not work, please import the project in an IDE as a Maven project and run the main() of ExchangeRateServiceApplication.kt

Sample curls:
-

Get daily and historical ECB published currency rates between any pair of ECB published currencies
`curl --location --request GET 'http://localhost:8081/v1/ecb-currency/pair-reference/rate?base-currency=GBP&converted-currency=EUR&number-of-days-in-the-past=10&latest=false' \
--data-raw ''`

Get statistics on count of the currencies searched for
`curl --location --request GET 'http://localhost:8081/v1/service-usage/currency-stats?currency=GBP'`

Get list of supported currencies by ecb
`curl --location --request GET 'http://localhost:8081/v1/ecb-currency?asOnDate=2022-05-06'`

Get URL to look up historical graph containing rates vs time data
`curl --location --request GET 'http://localhost:8081/v1/ecb-currency/pair-reference/graph/rate?base-currency=EUR&converted-currency=GBP'`

Convert currencies from one to another at latest or any past day rate
`curl --location --request POST 'http://localhost:8081/v1/ecb-currency/action/conversion' \
--header 'Content-Type: application/json' \
--data-raw '{
"sourceCurrency": "EUR",
"convertedCurrency": "INR",
"sourceCurrencyAmount": 10
}'`



