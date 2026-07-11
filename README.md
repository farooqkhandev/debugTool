# debugTool

A self-contained Android debug menu (shake → floating bug icon → tools). Ships **only in debug builds**. Release APKs contain no debug code.

## Project modules

| Module | Purpose |
|--------|---------|
| `:debugtool` | Publishable Android library (JitPack / Maven) |
| `:app` | Sample host app — demonstrates `DebugToolHost` wiring and the floating debug menu |

Open this folder in Android Studio, run `:app`, and tap the floating bug icon.

---

## Publish with JitPack (recommended)

Follows the standard [GitHub + JitPack](https://sagar0-0.medium.com/create-and-publish-your-android-open-source-library-in-minutes-ab2cb5627f7c) flow.

### 1. Push this project to GitHub

Repo: https://github.com/farooqkhandev/debugTool

### 2. Create a GitHub Release / tag

On GitHub → **Releases** → **Create a new release** → tag e.g. `1.0.0`.

### 3. Build on JitPack

1. Open https://jitpack.io
2. Paste `https://github.com/farooqkhandev/debugTool`
3. Look up → select tag `1.0.0` → wait until the build turns green

### 4. Consume in another app

**`settings.gradle.kts`:**

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

**`app/build.gradle.kts`:**

```kotlin
debugImplementation("com.github.farooqkhandev.debugTool:debugtool:1.0.0")
```

> Multi-module coordinate: `com.github.<user>.<repo>:<module>:<tag>`

Then implement `DebugToolHost` and call `DebugToolBootstrap.install(...)` (see sample `:app` module).

### Local sanity check before tagging

```bash
./gradlew :debugtool:assembleRelease
./gradlew :debugtool:publishToMavenLocal
```

---

## 1. What is debugTool?

`:debugtool` is one Gradle library module (`com.quadlogixs.debugtool`). It has **no dependency** on host app production modules.

| Sub-package | What it contains |
|-------------|------------------|
| `com.quadlogixs.debugtool.api` | Host contracts (`DebugToolHost`, `DebugToolConfig`) + registries (`DebugRuntimeRegistry`, `DebugNetworkRegistry`) |
| `com.quadlogixs.debugtool.core` | Network interceptors, stores, crash handler, Azure bug-report API |
| `com.quadlogixs.debugtool.ui` | All Compose screens, icons, internal UI components |
| `com.quadlogixs.debugtool` | Entry point (`DebugTool.install()`) |
| `com.quadlogixs.debugtool.bootstrap` | `DebugToolBootstrap.install()` — starts the module |

### Features

- Bug report to Azure DevOps (with screenshot)
- Network trace (Chucker), mock API, halt/edit API, API speed stats
- Environment switcher, encryption toggle
- Crash logs, logcat viewer
- UI tools: dynamic type, animation speed, layout grid, screen size simulator
- GPS spoofer, memory stats, recomposition stats, jank viewer
- QR debug scanner

### Important rules

1. Use **`debugImplementation`** — never `implementation` (release must not include this module).
2. `:debugTool` is **fully standalone** — publishable as AAR without your production modules.
3. Your app wires it through **`app/src/main`** (no-op) + **`app/src/debug`** (real code + bridge installer).
4. Production modules (`:common`, `:data`) keep small **bridge files** that no-op in release — the host connects them to `:debugTool` registries in `app/src/debug`.

---

## 2. How it is integrated in ZIslamicApp

### 2.1 Gradle

```kotlin
// settings.gradle.kts
include(":debugTool")

// app/build.gradle.kts
debugImplementation(project(":debugTool"))
```

### 2.2 Architecture (two layers)

```
┌──────────────────────────────────────────────────────────────────┐
│  :debugTool (standalone — safe to publish as AAR)                │
│                                                                  │
│  DebugTool.install()                                             │
│    → DebugRuntimeRegistry   (typography, location, loaders…)     │
│    → DebugNetworkRegistry   (mock, halt, chucker interceptors)  │
└───────────────────────────────┬──────────────────────────────────┘
                                │ connected in app/src/debug only
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│  Host app bridges (ZIslamic-specific)                            │
│                                                                  │
│  DebugToolBridgeInstaller.connectProductionBridges()             │
│    → common.DebugRuntimeHolder      (:common)                    │
│    → data.DebugNetworkBridge        (:data)                      │
└───────────────────────────────┬──────────────────────────────────┘
                                │ read by production code
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│  :common / :presentation / :data (never import :debugTool)       │
│  DebugRuntimeHolder.runtime · DebugNetworkBridge.contributor     │
└──────────────────────────────────────────────────────────────────┘
```

### 2.3 Debug vs release

```
RELEASE BUILD
  DebugToolIntegration → no-op (reflection fails safely)
  No debug menu, no interceptors, no :debugTool in APK

DEBUG BUILD
  DebugToolIntegration → DebugToolIntegrationImpl (reflection)
    → ZIslamicDebugToolHost          (app settings)
    → DebugToolBootstrap.install()   (:debugTool)
    → DebugToolBridgeInstaller       (connect registries → :common/:data)
    → debug menu + interceptors active
```

### 2.4 Files in this project

| File | Location | Role |
|------|----------|------|
| `DebugToolIntegration.kt` | `app/src/main` | No-op contract + reflection (release-safe) |
| `DebugToolIntegrationImpl.kt` | `app/src/debug` | FAB, menu, dialogs, startup wiring |
| `ZIslamicDebugToolHost.kt` | `app/src/debug` | Implements `DebugToolHost` (env, encryption, device ID) |
| `DebugToolBridgeInstaller.kt` | `app/src/debug` | Connects registries → `:common` / `:data` bridges |
| `ProductionDebugRuntimeBridge.kt` | `app/src/debug` | `DebugRuntimeRegistry` → `DebugRuntimeHolder` |
| `DebugToolBootstrap.kt` | `debugTool/bootstrap/` | Calls `DebugTool.install()` only |
| `DebugRuntimeRegistry.kt` | `debugtool/api/` | Holds runtime hooks (inside :debugTool) |
| `DebugNetworkRegistry.kt` | `debugtool/api/` | Holds OkHttp contributor (inside :debugTool) |
| `DebugRuntime.kt` | `common/.../debug/` | Production bridge — typography, location, loader |
| `DebugNetworkBridge.kt` | `data/.../debug/` | Production bridge — OkHttp interceptor slot |

### 2.5 Startup flow

```kotlin
// MainApplication.onCreate() — always called
DebugToolIntegration.onApplicationCreate(this)
```

In **debug** builds, `DebugToolIntegrationImpl` runs:

```kotlin
// 1. Build config from host adapter
val config = ZIslamicDebugToolHost.createConfig(host)

// 2. Start :debugTool (populates registries)
DebugToolBootstrap.install(application, config)

// 3. Connect to ZIslamic production bridges
DebugToolBridgeInstaller.connectProductionBridges()
```

What happens inside:

| Step | Where | What |
|------|-------|------|
| 1 | `DebugTool.install()` | `DebugToolCore.init()`, crash handler, mock store |
| 2 | `DebugRuntimeRegistry.install()` | Registers `AppDebugRuntime` hooks |
| 3 | `DebugNetworkRegistry.install()` | Registers mock/halt/chucker interceptors |
| 4 | `DebugToolBridgeInstaller` | Forwards registries → `DebugRuntimeHolder` + `DebugNetworkBridge` |

### 2.6 UI flow

1. **`MainActivity`** (debug): shake → show floating bug icon
2. **`DebugToolIntegration.WrapRootContent()`** — FAB, overlays, halt dialog
3. **`MyApp`**: `DebugToolIntegration.WrapMyApp()` — recomposition + memory tracking
4. User taps bug icon → debug menu opens

### 2.7 How other modules are affected (without importing debugTool)

| Module | Role |
|--------|------|
| **`:app`** | Integration + host adapter + bridge installer (`src/debug`) |
| **`:common`** | `DebugRuntimeHolder` — typography scale, loader suppression |
| **`:data`** | `DebugNetworkBridge` — OkHttp interceptor slot |
| **`:domain`** | Used only by `ZIslamicDebugToolHost` (save environment URL) |
| **`:presentation`** | Reads `DebugRuntimeHolder` for nav speed, loaders, mock GPS |
| **`:debugTool`** | Menus, dialogs, interceptors, Azure API — fully self-contained |

**Example:** User enables Dynamic Type → `DebugUiToolsStore` → `DebugRuntimeRegistry` → `DebugRuntimeHolder` → `sdp`/`textSdp` in `:common` scales all screens. `:presentation` never imports `:debugTool`.

### 2.8 Network interceptors (debug only)

`NetworkConfigModule` (`app/src/main`):

```kotlin
if (BuildConfig.DEBUG) {
    DebugNetworkBridge.contributor.contribute(interceptors, context)
}
```

Interceptor order: **Mock → Halt → Chucker → ApiSpeed → HttpLogging** (before encryption interceptors).

### 2.9 ZIslamicDebugToolHost

| `DebugToolHost` method | ZIslamic source |
|------------------------|-----------------|
| `environments()` | `HeaderInfoProvider` |
| `applyEnvironment()` | `CustomerLocalPrefsRepo` |
| `deviceId()` | `HeaderInfoProvider.getDeviceId()` |
| `isEncryptionEnabled()` | `EncManager()` (instantiated, not injected) |
| `appVersionName()` / `flavorName()` | `BuildConfig` |
| Azure PAT | `AZURE_DEVOPS_PAT` environment variable |

---

## 3. How to integrate in another project

### 3.1 What you copy

| Copy | Into your project |
|------|-------------------|
| `debugTool/` folder | Project root |
| `DebugToolIntegration.kt` | `app/src/main` |
| `DebugToolIntegrationImpl.kt` | `app/src/debug` |
| `YourAppDebugToolHost.kt` | `app/src/debug` |
| `DebugToolBridgeInstaller.kt` | `app/src/debug` (adapt from ZIslamic) |
| `ProductionDebugRuntimeBridge.kt` | `app/src/debug` (adapt from ZIslamic) |
| `DebugRuntime.kt` + helpers | Your shared UI module |
| `DebugNetworkBridge.kt` | Your network module |

### 3.2 Gradle

```kotlin
// settings.gradle.kts
include(":debugTool")

// app/build.gradle.kts
debugImplementation(project(":debugTool"))
// OR after Maven publish:
// debugImplementation("com.quadlogixs:debugtool:1.0.0")
```

`:debugTool` does not need your production modules on its classpath.

### 3.3 Integration checklist

| # | Task |
|---|------|
| 1 | Copy `debugTool/`, add to `settings.gradle.kts`, `debugImplementation` in app |
| 2 | Copy `DebugRuntime.kt` + `DebugNetworkBridge.kt` into your modules (section 3.5) |
| 3 | Add `DebugToolIntegration.kt` in `app/src/main` (reflection + no-op) |
| 4 | Create `YourAppDebugToolHost` implementing `DebugToolHost` in `app/src/debug` |
| 5 | Create `DebugToolIntegrationImpl` + `YourAppBridgeInstaller` in `app/src/debug` |
| 6 | `MainApplication.onCreate()` → `DebugToolIntegration.onApplicationCreate(this)` |
| 7 | `MainActivity` → shake detector + `WrapRootContent()` |
| 8 | `MyApp` → `WrapMyApp()` (optional, for recomposition/memory stats) |
| 9 | OkHttp builder → `DebugNetworkBridge.contributor.contribute(...)` when DEBUG |
| 10 | Verify debug menu works; verify release build has no debug code |

### 3.4 Minimum `onApplicationCreate` (debug impl)

```kotlin
override fun onApplicationCreate(application: Application) {
    val host = /* YourAppDebugToolHost from Hilt */
    val config = YourAppDebugToolHost.createConfig(host)

    DebugToolBootstrap.install(application, config)
    YourAppBridgeInstaller.connectProductionBridges()
}
```

### 3.5 Production bridge files (in your modules)

**`DebugRuntime.kt`** — shared UI module (`:common` equivalent):

```kotlin
interface DebugRuntime {
    val typographyScale: StateFlow<Float>
    fun navAnimationDurationMillis(baseMs: Int): Int
    fun mockLocation(): Pair<Double, Double>? = null
    fun isLoaderSuppressed(): Boolean = false
    fun useDebugNavAnimations(): Boolean = false
}

object NoOpDebugRuntime : DebugRuntime {
    override val typographyScale = MutableStateFlow(1f)
    override fun navAnimationDurationMillis(baseMs: Int) = baseMs
}

object DebugRuntimeHolder {
    @Volatile var runtime: DebugRuntime = NoOpDebugRuntime
}

fun installDebugRuntime(runtime: DebugRuntime) {
    DebugRuntimeHolder.runtime = runtime
}
```

**`DebugNetworkBridge.kt`** — network module (`:data` equivalent):

```kotlin
fun interface DebugNetworkContributor {
    fun contribute(interceptors: MutableList<Interceptor>, context: Context)
}

object DebugNetworkBridge {
    @Volatile var contributor: DebugNetworkContributor = DebugNetworkContributor { _, _ -> }
}

fun installDebugNetworkContributor(contributor: DebugNetworkContributor) {
    DebugNetworkBridge.contributor = contributor
}
```

### 3.6 Bridge installer (in `app/src/debug`)

Connect `:debugTool` registries to your production bridges:

```kotlin
object YourAppBridgeInstaller {

    fun connectProductionBridges() {
        // Runtime: debugTool → your common module
        DebugRuntimeRegistry.get()?.let { hooks ->
            installDebugRuntime(YourProductionDebugRuntimeBridge(hooks))
        }

        // Network: debugTool → your data module
        installDebugNetworkContributor { interceptors, context ->
            DebugNetworkRegistry.contributor.contribute(interceptors, context)
        }
    }
}
```

`YourProductionDebugRuntimeBridge` implements your `DebugRuntime` interface and delegates to `DebugRuntimeHooks` from the registry. Copy `ProductionDebugRuntimeBridge.kt` from ZIslamicApp as a template.

### 3.7 `DebugToolHost` interface

```kotlin
interface DebugToolHost {
    fun environments(): List<DebugEnvironment>
    suspend fun currentEnvironment(): String
    suspend fun applyEnvironment(url: String)
    fun appVersionName(): String
    fun flavorName(): String
    fun isEncryptionEnabled(context: Context): Boolean
    suspend fun setEncryptionEnabled(context: Context, enabled: Boolean)
    fun deviceId(): String
}
```

### 3.8 Do's and Don'ts

| Do | Don't |
|----|-------|
| `debugImplementation(project(":debugTool"))` | `implementation(project(":debugTool"))` |
| Bridge installer in `app/src/debug` | Import `:debugTool` from `:common` / `:data` / `:presentation` |
| `DebugToolBootstrap` then `connectProductionBridges()` | Skip bridge installer (production hooks won't work) |
| Instantiate `EncManager()` directly | `@Inject EncManager` without a Hilt `@Provides` |
| Set `AZURE_DEVOPS_PAT` for bug reports | Hardcode secrets in the module |

---

## 4. Quick reference

### Azure PAT

```bash
set AZURE_DEVOPS_PAT=your_token_here
```

### Troubleshooting

| Problem | Fix |
|---------|-----|
| No bug icon | Debug build + shake device |
| Dynamic type / location not working | Call `connectProductionBridges()` after bootstrap |
| Interceptors not active | `DebugNetworkBridge.contributor.contribute()` in OkHttp builder when DEBUG |
| Hilt error on host adapter | Only inject Hilt types; instantiate others (e.g. `EncManager()`) |
| Bug report fails | Set `AZURE_DEVOPS_PAT` |
| Halt API blocked | Firestore licence + valid `deviceId()` |

### Verify before merge

- [ ] `:debugTool:compileDebugKotlin` passes
- [ ] `:app:compileQADebugKotlin` passes
- [ ] `:app:compileQAReleaseKotlin` passes (no `:debugTool` on release classpath)
- [ ] Debug: shake → menu → Chucker opens
- [ ] Debug: dynamic type / environment switcher work
- [ ] Release: no debug icon, no interceptors

---

## 5. Publish as open-source library (JitPack)

Configured per the [JitPack Android library guide](https://sagar0-0.medium.com/create-and-publish-your-android-open-source-library-in-minutes-ab2cb5627f7c):

| File | Role |
|------|------|
| `debugtool/build.gradle.kts` | `maven-publish` + `group = com.github.farooqkhandev` |
| `jitpack.yml` | JDK 17 + `:debugtool:publishToMavenLocal` |

### Steps

1. Commit & push this project to https://github.com/farooqkhandev/debugTool
2. Create a **GitHub Release** with tag `1.0.0` (or bump `debugtool.publish.version` first)
3. Open https://jitpack.io → look up the repo → wait for a green build
4. Add dependency:

```kotlin
debugImplementation("com.github.farooqkhandev.debugTool:debugtool:1.0.0")
```

### Optional: GitHub Packages

Credentials in `~/.gradle/gradle.properties` (`gpr.user` / `gpr.key`), then:

```bash
./gradlew :debugtool:publishReleasePublicationToGitHubPackagesRepository
```
