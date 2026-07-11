# debugTool

Standalone Android debug menu library (shake → floating bug icon → tools).

Add it with **`debugImplementation` only** so release builds stay clean.

**Package:** `com.quadlogixs.debugtool`  
**Repo:** https://github.com/farooqkhandev/debugTool

---

## Features

- Bug report to Azure DevOps (screenshot + assignees)
- Network trace (Chucker), mock API, halt / edit API, API speed stats
- Environment switcher, encryption toggle
- Crash logs, logcat viewer
- UI tools: dynamic type, animation speed, layout grid, screen size simulator
- GPS spoofer, memory / recomposition / jank stats
- QR debug scanner

---

## Quick start (use in your app)

### 1. Add JitPack

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

### 2. Add the dependency

`app/build.gradle.kts`:

```kotlin
debugImplementation("com.github.farooqkhandev:debugtool:1.0.0")
```

> Use **`debugImplementation`**, never `implementation`.

If Gradle cannot resolve the artifact, open https://jitpack.io/#farooqkhandev/debugTool and wait for tag `1.0.0` to turn green, or publish locally:

```bash
./gradlew :debugtool:publishToMavenLocal
```

Then add `mavenLocal()` to your repositories.

### 3. Implement `DebugToolHost`

Provide app-specific settings (environments, device id, encryption, version):

```kotlin
class MyDebugToolHost(
    private val prefs: SharedPreferences,
) : DebugToolHost {

    override fun environments() = listOf(
        DebugEnvironment("Dev", "https://dev.example.com"),
        DebugEnvironment("QA", "https://qa.example.com"),
    )

    override suspend fun currentEnvironment(): String =
        prefs.getString("env_url", environments().first().url).orEmpty()

    override suspend fun applyEnvironment(url: String) {
        prefs.edit().putString("env_url", url).apply()
    }

    override fun appVersionName(): String = BuildConfig.VERSION_NAME
    override fun flavorName(): String = BuildConfig.FLAVOR
    override fun deviceId(): String = /* your device id */

    override fun isEncryptionEnabled(context: Context): Boolean = true
    override suspend fun setEncryptionEnabled(context: Context, enabled: Boolean) { /* optional */ }
}
```

See the full sample in `:app` → `SampleDebugToolHost`.

### 4. Install at app startup

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            val host = MyDebugToolHost(/* ... */)
            DebugToolBootstrap.install(
                application = this,
                config = DebugToolConfig(
                    host = host,
                    azure = AzureDevOpsConfig(
                        organization = "your-org",
                        project = "your-project",
                        areaPath = "your-project",
                        patProvider = AzurePatProvider {
                            System.getenv("AZURE_DEVOPS_PAT").orEmpty()
                        },
                    ),
                    assignees = listOf(
                        AssignedTo(name = "Dev", emailAddress = "dev@example.com"),
                    ),
                    azureLabel = "your-project/debugTool",
                ),
            )
        }
    }
}
```

### 5. Show the menu (Compose)

Copy the floating bug icon + dialog wiring from the sample:

`app/src/main/java/.../sample/MainActivity.kt`

Typical flow:

1. Optional: start `ShakeDetector` to toggle the icon / open Chucker  
2. Show a floating bug FAB  
3. On tap → `ShowDebugToolMenuDialog` and feature dialogs  

### 6. Hook OkHttp (network tools)

After `DebugToolBootstrap.install(...)`, add debug interceptors when building OkHttp:

```kotlin
if (BuildConfig.DEBUG) {
    DebugNetworkRegistry.contributor.contribute(interceptors, context)
}
```

Order inside the library: **Mock → Halt → Chucker → ApiSpeed**.

### 7. (Optional) Connect runtime hooks

If you want dynamic type, mock GPS, loader suppression, or nav animation speed in your own UI modules **without** importing debugTool there:

1. Keep a small no-op bridge in your shared module (e.g. `DebugRuntimeHolder`)
2. In `app` debug code, forward `DebugRuntimeRegistry` into that bridge after install

Minimal pattern:

```kotlin
DebugRuntimeRegistry.get()?.let { hooks ->
    // install into your app’s DebugRuntimeHolder
}
```

See `DebugRuntimeHooks` / `DebugRuntimeRegistry` in `com.quadlogixs.debugtool.api`.

---

## Try the sample app

This repo includes `:app` as a working host.

1. Open the `debugTool` project in Android Studio  
2. Run `:app`  
3. Tap the floating bug icon  

---

## Project layout

| Module | Purpose |
|--------|---------|
| `:debugtool` | Publishable library |
| `:app` | Sample host (reference integration) |

| Package | Contents |
|---------|----------|
| `api` | `DebugToolHost`, `DebugToolConfig`, registries |
| `bootstrap` | `DebugToolBootstrap.install()` |
| `core` | Interceptors, stores, crash handler, Azure API |
| `ui` | Compose menus and dialogs |

---

## Rules

| Do | Don’t |
|----|--------|
| `debugImplementation("…:debugtool:…")` | `implementation(...)` |
| Install only in debug / `app` wiring | Import the library from production feature modules if you can avoid it |
| Set `AZURE_DEVOPS_PAT` for bug reports | Hardcode PATs in source |
| Call `DebugNetworkRegistry.contributor.contribute(...)` for network tools | Expect Chucker/mock/halt without OkHttp wiring |

---

## Azure bug reports

```bash
# Windows
set AZURE_DEVOPS_PAT=your_token_here

# macOS / Linux
export AZURE_DEVOPS_PAT=your_token_here
```

Pass org / project / area path via `AzureDevOpsConfig` when building `DebugToolConfig`.

---

## Publish (maintainers)

1. Push to GitHub  
2. Create a release tag (e.g. `1.0.0`)  
3. Build on https://jitpack.io → wait for green  
4. Consumers use:

```kotlin
debugImplementation("com.github.farooqkhandev:debugtool:1.0.0")
```

Local check before tagging:

```bash
./gradlew :debugtool:assembleRelease
./gradlew :debugtool:publishToMavenLocal
```

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Dependency not found | Wait for JitPack green build, or use `mavenLocal()` |
| No bug icon | Debug build + sample FAB / shake wiring |
| Chucker / mock / halt missing | Call `DebugNetworkRegistry.contributor.contribute(...)` on OkHttp |
| Dynamic type / mock location unused | Forward `DebugRuntimeRegistry` into your app bridge |
| Bug report fails | Set `AZURE_DEVOPS_PAT` and valid Azure config |
| Duplicate Chucker classes | Don’t also add `library-no-op` on the same classpath; prefer one Chucker artifact |
