package org.masa.installer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    var profile by rememberSaveable {
        mutableStateOf(
            NetworkProfile(
                domainId = "",
                coordinateProfile = "TBD",
                identityProfile = "TBD",
                messageProfile = "TBD",
                routingProfile = "TBD",
                cryptographicProfile = "TBD",
                classA = true,
                classB = true,
            ),
        )
    }
    var previewJson by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(previewJson) }
                ?: error("Could not open the selected destination")
        }.onSuccess {
            Toast.makeText(context, "Draft configuration exported", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Export failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("MASA Network Setup", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Draft installer workflow for MASA Networking Domain 0.4.4-draft.")
        Text(
            "This app exports a draft-only configuration. It does not install or activate a MASA networking client, and it does not claim production interoperability.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedTextField(
            value = profile.domainId,
            onValueChange = { profile = profile.copy(domainId = it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Authoritative BoxelDomainID (required)") },
            supportingText = { Text("W context must be bound to this authoritative ID. Do not invent a W value.") },
            singleLine = true,
        )

        ProfileField("Coordinate profile", profile.coordinateProfile) {
            profile = profile.copy(coordinateProfile = it)
        }
        ProfileField("Identity profile", profile.identityProfile) {
            profile = profile.copy(identityProfile = it)
        }
        ProfileField("Message profile", profile.messageProfile) {
            profile = profile.copy(messageProfile = it)
        }
        ProfileField("Routing profile", profile.routingProfile) {
            profile = profile.copy(routingProfile = it)
        }
        ProfileField("Cryptographic profile", profile.cryptographicProfile) {
            profile = profile.copy(cryptographicProfile = it)
        }

        Text("Communication classes", style = MaterialTheme.typography.titleMedium)
        ClassOption("Class A — within one Boxel", profile.classA) {
            profile = profile.copy(classA = it)
        }
        ClassOption("Class B — across Boxel boundaries", profile.classB) {
            profile = profile.copy(classB = it)
        }

        validationMessage?.let {
            Text(
                text = it,
                color = Color(0xFFB00020),
                modifier = Modifier
                    .background(Color(0x25B00020))
                    .padding(8.dp)
                    .fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = {
                when {
                    profile.domainId.isBlank() -> {
                        validationMessage = "Enter the authoritative BoxelDomainID before exporting."
                    }
                    !profile.classA && !profile.classB -> {
                        validationMessage = "Select at least one communication class."
                    }
                    else -> {
                        validationMessage = null
                        previewJson = profile.toJson().toString(2)
                        createDocument.launch("masa-network-config.json")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Export draft configuration")
        }

        Button(
            onClick = {
                previewJson = profile.toJson().toString(2)
                Toast.makeText(context, "Draft preview refreshed", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Preview JSON")
        }

        if (previewJson.isNotBlank()) {
            Text(
                text = previewJson,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp),
            )
        }

        TextButton(
            onClick = {
                profile = NetworkProfile(
                    domainId = "",
                    coordinateProfile = "TBD",
                    identityProfile = "TBD",
                    messageProfile = "TBD",
                    routingProfile = "TBD",
                    cryptographicProfile = "TBD",
                    classA = true,
                    classB = true,
                )
                previewJson = ""
                validationMessage = null
            },
            modifier = Modifier.align(Alignment.End),
        ) {
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
        supportingText = { Text("Approved profile ID or TBD") },
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
