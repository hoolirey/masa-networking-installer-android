# MASA Networking Installer for Android

An Android configuration-builder starter for MASA Networking Domain 0.4.4-draft. It lets an operator enter the authoritative `BoxelDomainID`, select Class A and/or Class B, record profile identifiers, and export a JSON draft using Android's system document picker.

## Important scope and safety

This version **does not install, download, activate, or connect a MASA networking client**. The MASA summary does not provide an approved client artifact, final wire format, or complete interoperable profiles. The app therefore does not invent those details or claim interoperability. Profile fields default to `TBD`, exports are marked `draft`, and the `productionInteroperabilityClaim` field is always `false`.

The `W` context namespace is populated only from the required authoritative `BoxelDomainID`; the app does not generate a W value. Do not use a placeholder domain ID as an operational identity.

Before adding client installation or connection features, define and approve the client package/distribution mechanism, configuration schema, profile identifiers, credential provisioning and key lifecycle, and the behavior required for Android networking (for example, whether a user-approved `VpnService` is needed). Android package installation is subject to platform user-consent and package-source rules; this app does not bypass them.

## Build

- Android Studio with JDK 17
- Android SDK Platform 35
- Gradle 8.9 or newer (Android Gradle Plugin 8.7.3)

Open the repository in Android Studio and sync Gradle, or from the project root run:

```sh
gradle assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/`.

## Export format

The exported JSON contains the draft domain version, authoritative Boxel domain context, selected communication classes, profile identifiers (including any unresolved `TBD`s), and an explicit no-interoperability-claim marker. It contains no credentials or private keys.
