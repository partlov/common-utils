package org.opensearch.commons.alerting.alerts

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException
import java.time.Instant

data class AlertError(val timestamp: Instant, val message: String) : Writeable, ToXContent {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readInstant(), // timestamp
        sin.readString() // message
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeInstant(timestamp)
        out.writeString(message)
    }
    companion object {

        const val TIMESTAMP_FIELD = "timestamp"
        const val MESSAGE_FIELD = "message"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): AlertError {

            lateinit var timestamp: Instant
            lateinit var message: String

            ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    TIMESTAMP_FIELD -> timestamp = requireNotNull(xcp.instant())
                    MESSAGE_FIELD -> message = xcp.text()
                }
            }
            return AlertError(timestamp = timestamp, message = message)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): AlertError {
            return AlertError(sin)
        }
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .optionalTimeField(TIMESTAMP_FIELD, timestamp)
            .field(MESSAGE_FIELD, message)
            .endObject()
    }
}
