package org.masa.installer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SetupScreen()
                }
            }
        }
    }
}

@Composable
private fun SetupScreen() {
    val context = LocalContext.current
    var domainId by rememberSaveable { mutableStateOf("") }
    var coordinateProfile by rememberSaveable { mutableStateOf("TBD") }
    var identityProfile by rememberSaveable { mutableStateOf("TBD") }
    var messageProfile by rememberSaveable { mutableStateOf("TBD") }
    var routingProfile by rememberSaveable { mutableStateOf("TBD") }
    var cryptographicProfile by rememberSaveable { mutableStateOf("TBD") }
    var classA by rememberSaveable { mutableStateOf(true) }
    var classB by rememberSaveable { mutableStateOf(true) }
    var pendingJson by remember { mutableStateOf("") }

    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(pendingJson) }
                ?: error("Could not open the selected destination")
        }.onSuccess {
            Toast.makeText(context, "Draft configuration exported", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Export failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("MASA Network Setup", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Create a draft client configuration for MASA Networking Domain 0.4.4-draft.")
        Text(
            "Draft only — this app does not install or activate a networking client. Configure approved profiles before production use; unresolved values remain TBD.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedTextField(
            value = domainId,
            onValueChange = { domainId = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Authoritative BoxelDomainID (required)") },
            supportingText = { Text("The domain context W must be bound to this authoritative ID. Do not invent a W value.") },
            singleLine = true,
        )
        ProfileField("Coordinate profile", coordinateProfile) { coordinateProfile = it }
        ProfileField("Identity profile", identityProfile) { identityProfile = it }
        ProfileField("Message profile", messageProfile) { messageProfile = it }
        ProfileField("Routing profile", routingProfile) { routingProfile = it }
        ProfileField("Cryptographic profile", cryptographicProfile) { cryptographicProfile = it }

        Text("Communication classes", style = MaterialTheme.typography.titleMedium)
        ClassOption("Class A — within one Boxel", classA) { classA = it }
        ClassOption("Class B — across Boxel boundaries", classB) { classB = it }

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = {
                if (domainId.isBlank()) {
                    Toast.makeText(context, "Enter the authoritative BoxelDomainID first", Toast.LENGTH_SHORT).show()
                } else if (!classA && !classB) {
                    Toast.makeText(context, "Select at least one communication class", Toast.LENGTH_SHORT).show()
                } else {
                    pendingJson = makeConfiguration(
                        domainId.trim(), coordinateProfile, identityProfile, messageProfile,
                        routingProfile, cryptographicProfile, classA, classB,
                    ).toString(2)
                    createDocument.launch("masa-network-config.json")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Export draft configuration")
        }
        TextButton(onClick = {
            domainId = ""
            coordinateProfile = "TBD"
            identityProfile = "TBD"
            messageProfile = "TBD"
            routingProfile = "TBD"
            cryptographicProfile = "TBD"
            classA = true
            classB = true
        }, modifier = Modifier.align(Alignment.End)) {
            Text("Reset")
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        supportingText = { Text("Enter an approved profile identifier, or leave TBD unresolved.") },
        singleLine = true,
    )
}

@Composable
private fun ClassOption(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

private fun makeConfiguration(
    domainId: String,
    coordinateProfile: String,
    identityProfile: String,
    messageProfile: String,
    routingProfile: String,
    cryptographicProfile: String,
    classA: Boolean,
    classB: Boolean,
): JSONObject = JSONObject().apply {
    put("configurationSchema", "masa-network-client-config/1")
    put("domainVersion", "0.4.4-draft")
    put("configurationStatus", "draft")
    put("boxelDomainId", domainId)
    put("coordinateContextNamespaceW", domainId)
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
    put("notes", "Draft configuration only. Complete and approve required MASA profiles before production interoperability.")
}
