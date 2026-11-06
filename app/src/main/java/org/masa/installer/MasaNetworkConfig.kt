package org.masa.installer

import org.json.JSONArray
import org.json.JSONObject

data class NetworkProfile(
    val domainId: String,
    val coordinateProfile: String = "TBD",
    val identityProfile: String = "TBD",
    val messageProfile: String = "TBD",
    val routingProfile: String = "TBD",
    val cryptographicProfile: String = "TBD",
    val classA: Boolean = true,
    val classB: Boolean = true,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("configurationSchema", "masa-network-client-config/1")
        put("domainVersion", "0.4.4-draft")
        put("configurationStatus", "draft")
        put("boxelDomainId", domainId.trim())
        put("coordinateContextNamespaceW", domainId.trim())
        put("communicationClasses", JSONArray().apply {
            if (classA) put("A")
            if (classB) put("B")
        })
        put("profiles", JSONObject().apply {
            put("coordinate", coordinateProfile.trim().ifBlank { "TBD" })
            put("identity", identityProfile.trim().ifBlank { "TBD" })
            put("message", messageProfile.trim().ifBlank { "TBD" })
            put("routing", routingProfile.trim().ifBlank { "TBD" })
            put("cryptographic", cryptographicProfile.trim().ifBlank { "TBD" })
        })
        put("productionInteroperabilityClaim", false)
        put("notes", "Draft configuration only. Complete and approve all required MASA profiles before production interoperability.")
    }
}
