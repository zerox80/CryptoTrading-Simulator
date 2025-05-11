package org.zerox80.coingeckowebapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zerox80.coingeckowebapp.client.CoinGeckoApiClient;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;

import java.util.ArrayList;
import java.util.List;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CryptoServiceTest {

    @Mock
    private CoinGeckoApiClient apiClient;

    @InjectMocks
    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTopCryptocurrencies_Success() {
        List<CryptoCurrency> mockCryptoList = new ArrayList<>();
        mockCryptoList.add(new CryptoCurrency("bitcoin", "Bitcoin", "btc", "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579", 50000.0, 1000000000.0, 2.5));
        mockCryptoList.add(new CryptoCurrency("ethereum", "Ethereum", "eth", "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880", 3000.0, 500000000.0, -1.0));

        when(apiClient.getCoins("usd", 10)).thenReturn(Mono.just(mockCryptoList));

        List<CryptoCurrency> result = cryptoService.getTopCryptocurrencies("usd", 10).block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Bitcoin", result.get(0).getName());
        assertEquals("ethereum", result.get(1).getId());

        verify(apiClient, times(1)).getCoins("usd", 10);
    }

    @Test
    void testGetTopCryptocurrencies_EmptyResponse() {
        when(apiClient.getCoins("usd", 10)).thenReturn(Mono.just(new ArrayList<>()));

        List<CryptoCurrency> result = cryptoService.getTopCryptocurrencies("usd", 10).block();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(apiClient, times(1)).getCoins("usd", 10);
    }

    @Test
    void testGetTopCryptocurrencies_ApiThrowsException() {
        when(apiClient.getCoins("usd", 10)).thenReturn(Mono.error(new RuntimeException("API Error")));

        assertThrows(RuntimeException.class, () ->
                cryptoService.getTopCryptocurrencies("usd", 10).block());

        verify(apiClient, times(1)).getCoins("usd", 10);
    }
}