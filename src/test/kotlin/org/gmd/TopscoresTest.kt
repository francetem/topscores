package org.gmd

import com.fasterxml.jackson.databind.ObjectMapper
import org.gmd.model.Game
import org.gmd.model.Metric
import org.gmd.model.Score
import org.gmd.model.TournamentStatus
import org.gmd.repository.GameRepository
import org.gmd.repository.GameRepositoryForTesting
import org.gmd.service.GameService
import org.gmd.service.GameServiceImpl
import org.gmd.service.alg.AdderMemberRatingAlgorithm
import org.gmd.service.alg.ELOMemberRatingAlgorithm
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.Charset
import java.util.*


@WebMvcTest(Topscores::class)
@RunWith(SpringRunner::class)
class TopscoresTest {

    @Autowired
    private val mockMvc: MockMvc? = null

    @Configuration
    open class AppConfig {

        //FIXME: This needed due to not being able to use @MockBean directly to get mocks
        //of the required beens in this test. There are several solutions that need to be
        //tested, one of them updating to the latest version of Mockito that does not have
        //this problem and it's able to mock any class despite being final or private.

        /*
            val repo = mock(GameRepository::class.java)
            `when`(repo!!.addGame(any()))
                    .then({ invocation -> invocation.getArgumentAt(0, Game::class.java) })
            return repo
         */

        private val repository = GameRepositoryForTesting(listOf(TestData.patxanga()))

        @Bean
        open fun authentication(): BasicConfiguration {
            return BasicConfiguration(EnvProviderForTesting(mapOf("token:user" to "pwd")))
        }

        @Bean
        open fun gameRepository(): GameRepository {
            return repository
        }

        @Bean
        open fun gameService(): GameService {
            val gameService = GameServiceImpl(repository, AdderMemberRatingAlgorithm(), ELOMemberRatingAlgorithm())
            return gameService
        }

        @Bean
        open fun controller(): Topscores {
            return Topscores()
        }
    }

    @Test
    @Throws(Exception::class)
    fun addGameShouldReturnTheSameJson() {
        val request = post("/games/add")
                .content(TestData.patxanga)
                .contentType("application/json")
                .header("Authorization", basicAuthHeader("user", "pwd"))

        this.mockMvc!!.perform(request).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(TestData.patxanga, true))
    }

    @Test
    @Throws(Exception::class)
    fun addGameShouldCreateTimestampForGame() {
        val request = post("/games/add")
                .content(TestData.patxanga_no_timestamp)
                .contentType("application/json")
                .header("Authorization", basicAuthHeader("user", "pwd"))

        this.mockMvc!!.perform(request).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(TimestampExists()))
    }

    @Test
    @Throws(Exception::class)
    fun addGameShouldSupportOptionalMetricsAndTags() {
        val request = post("/games/add")
                .content(TestData.mariokart)
                .contentType("application/json")
                .header("Authorization", basicAuthHeader("user", "pwd"))

        this.mockMvc!!.perform(request).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(TestData.mariokart, false))
    }
    
    @Test
    @Throws(Exception::class)
    fun scoresShouldReturnAggregatedDataByAccount() {
        val expected = """
        {
            "metrics": [
                {
                    "member": "Arnau",
                    "metrics": [
                        {
                            "name": "gols",
                            "value": 1
                        }
                    ]
                },
                {
                    "member": "Guillem",
                    "metrics": [
                        {
                            "name": "gols",
                            "value": 2
                        }
                    ]
                },
                {
                    "member": "Ramon",
                    "metrics": [
                        {
                            "name": "gols",
                            "value": 2
                        }
                    ]
                }
            ],
            "scores": [
                {
                    "member": "Ramon",
                    "score": 1
                },
                {
                    "member": "Arnau",
                    "score": 1
                },
                {
                    "member": "Uri",
                    "score": 0
                },
                {
                    "member": "Guillem",
                    "score": 0
                }
            ]
        }
        """
        val request = get("/scores/patxanga/players")
                .header("Authorization", basicAuthHeader("user", "pwd"))

        this.mockMvc!!.perform(request).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(expected))
    }

    private fun basicAuthHeader(user: String, password: String): String {
        return "Basic " + Base64.getEncoder().encodeToString("$user:$password".toByteArray(Charset.defaultCharset()))
    }
}

class TimestampExists : BaseMatcher<String>() {
    override fun describeTo(description: Description?) {
        description!!.appendText("has a timestamp")
    }

    override fun matches(item: Any?): Boolean {
        if (item is String) {
            return ObjectMapper().readValue(item, Game::class.java).timestamp!! > 0
        }

        return false
    }
}

