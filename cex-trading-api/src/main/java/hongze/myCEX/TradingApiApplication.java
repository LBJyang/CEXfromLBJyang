package hongze.myCEX;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

import hongze.myCEX.client.RestClient;

@SpringBootApplication
public class TradingApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(TradingApiApplication.class, args);
	}

	public RestClient createTradingEngineRestClient(
			@Value("#{exchangeConfiguration.apiEndpoints.tradingEngineApi}") String tradingEngineApiEndpoint,
			@Autowired ObjectMapper objectMapper) {
		return new RestClient.Builder(tradingEngineApiEndpoint).build(objectMapper);
	}
}
