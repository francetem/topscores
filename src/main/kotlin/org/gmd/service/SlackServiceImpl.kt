package org.gmd.service

import org.gmd.EnvProvider
import org.gmd.repository.SlackRepository
import org.gmd.slack.SlackExecutorProvider
import org.gmd.slack.SlackTeamAuth
import org.springframework.stereotype.Component

@Component
class SlackServiceImpl(private val env: EnvProvider,
                       private val slackExecutorProvider: SlackExecutorProvider,
                       private val slackRepository: SlackRepository) : SlackService {

    override fun oauth(code: String): SlackTeamAuth {
        val slackSecret = env.getEnv()[EnvProvider.SLACK_CLIENT_SECRET]
        val slackClientId = env.getEnv()[EnvProvider.SLACK_CLIENT_ID]
        val response = slackExecutorProvider.oauthExecutor()(slackClientId!!, slackSecret!!, code)
        slackRepository.storeAuth(response)
        return response
    }

    override fun postWebApi(teamName: String, method: String, jsonBody: String): String {
        val auth = slackRepository.getAuth(teamName)
        return slackExecutorProvider.webApiExecutor()(method, jsonBody, auth.accessToken)
    }

    override fun getWebApi(teamName: String, method: String): List<String> {
        val auth = slackRepository.getAuth(teamName)
        return slackExecutorProvider.webApiPaginatedExecutor()(method, auth.accessToken)
    }

}