package org.gmd.slack

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

class SlackWebApiResponse() {

    var ok: Boolean = false
    var error: String? = null

    @JsonProperty("response_metadata")
    var responseMetadata: SlackWebApiResponseMetadata? = null

    constructor(ok: Boolean, error: String, responseMetadata: SlackWebApiResponseMetadata) : this() {
        this.ok = ok
        this.error = error
        this.responseMetadata = responseMetadata
    }

    companion object {
        fun fromJson(json: String): SlackWebApiResponse {
            return ObjectMapper().readValue(json, SlackWebApiResponse::class.java)
        }
    }

    override fun toString(): String {
        return "SlackWebApiResponse(ok=$ok, error=$error, responseMetadata=$responseMetadata)"
    }
}

