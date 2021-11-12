package no.nav.syfo.api

import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.azuread.AzureAdToken
import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.MultiValueMap
import java.text.ParseException
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LocalApplication::class])
class AzureTokenCacheTest {

    @MockBean
    private lateinit var azureAdTokenConsumer: AzureAdTokenConsumer

    private val pdlScopeClientId = "pdl"
    private val anotherScopeClientId = "another"
    private lateinit var requestEntityPDL: HttpEntity<MultiValueMap<String, String>>
    private lateinit var requestEntityAnother: HttpEntity<MultiValueMap<String, String>>

    @BeforeEach
    @Throws(ParseException::class)
    fun setup() {
        Mockito.`when`(azureAdTokenConsumer.getSystemToken(pdlScopeClientId)).thenCallRealMethod()
        Mockito.`when`(azureAdTokenConsumer.systemTokenRequestEntity(pdlScopeClientId)).thenCallRealMethod()
        requestEntityPDL = azureAdTokenConsumer.systemTokenRequestEntity(pdlScopeClientId)
        Mockito.`when`(azureAdTokenConsumer.getSystemToken(anotherScopeClientId)).thenCallRealMethod()
        Mockito.`when`(azureAdTokenConsumer.systemTokenRequestEntity(anotherScopeClientId)).thenCallRealMethod()
        requestEntityAnother = azureAdTokenConsumer.systemTokenRequestEntity(anotherScopeClientId)
    }

    @AfterEach
    fun tearDown() {
        AzureAdTokenConsumer.tokenCache.clear()
    }


    @Test
    fun `token is cached`() {
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 0)
        // Two thenReturn-statements to get different result on first and second invocation
        Mockito.`when`(azureAdTokenConsumer.getToken(requestEntityPDL)).thenReturn(
            AzureAdToken(
                accessToken = "first",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        ).thenReturn(
            AzureAdToken(
                accessToken = "second",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        )
        val token = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        assertEquals(token, "first")
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 1)
        val anotherToken = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        assertEquals(anotherToken, "first")
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 1)
     }

    @Test
    fun `expired token is renewed`() {
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 0)
        // Two thenReturn-statements to get different result on first and second invocation
        Mockito.`when`(azureAdTokenConsumer.getToken(requestEntityPDL)).thenReturn(
            AzureAdToken(
                accessToken = "first",
                expires = LocalDateTime.now(),
            )
        ).thenReturn(
            AzureAdToken(
                accessToken = "second",
                expires = LocalDateTime.now(),
            )
        )
        val token = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        assertEquals(token, "first")
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 1)
        val anotherToken = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        assertEquals(anotherToken, "second")
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 1)
    }

    @Test
    fun `different scope ids get separate tokens`() {
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 0)
        Mockito.`when`(azureAdTokenConsumer.getToken(requestEntityPDL)).thenReturn(
            AzureAdToken(
                accessToken = "firstPDL",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        ).thenReturn(
            AzureAdToken(
                accessToken = "secondPDL",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        )
        Mockito.`when`(azureAdTokenConsumer.getToken(requestEntityAnother)).thenReturn(
            AzureAdToken(
                accessToken = "firstAnother",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        ).thenReturn(
            AzureAdToken(
                accessToken = "secondAnother",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        )
        val tokenPDL = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        assertEquals(tokenPDL, "firstPDL")
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 1)

        val tokenAnother = azureAdTokenConsumer.getSystemToken(anotherScopeClientId)
        assertEquals(tokenAnother, "firstAnother")
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 2)

        val newTokenPDL = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        assertEquals(newTokenPDL, "firstPDL")
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 2)

        val newTokenAnother = azureAdTokenConsumer.getSystemToken(anotherScopeClientId)
        assertEquals(newTokenAnother, "firstAnother")
        assertEquals(AzureAdTokenConsumer.tokenCache.size, 2)
    }
}
