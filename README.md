# debugTool

Standalone Android debug menu library (floating bug FAB → tools).

**Package:** `com.quadlogixs.debugtool` / `com.quadlogixs.debugtool.hooks`  
**Repo:** https://github.com/farooqkhandev/debugTool  
**Version:** 1.1.0

---

## Features

- Bug report to Azure DevOps (screenshot + assignees)
- Network trace (Chucker), mock API, halt / edit API, API speed stats
- Environment switcher, encryption toggle
- Crash logs, logcat viewer
- UI tools: dynamic type, animation speed, layout grid, screen size simulator
- GPS spoofing, memory / recomposition / jank stats
- QR debug scanner
- Always-safe **hooks** module for release-safe runtime flags + OkHttp wiring

---

## Quick start

### 1. JitPack + dependencies

```kotlin
// settings.gradle.kts — repositories
maven(url = "https://jitpack.io")

// app/build.gradle.kts
implementation("com.github.farooqkhandev:debugtool-hooks:1.1.0") // all build types
debugImplementation("com.github.farooqkhandev:debugtool:1.1.0")  // debug only
```

### 2. Implement `DebugToolHost` + install (debug)

```kotlin
class MyDebugToolHost(...) : DebugToolHost { /* environments, deviceId, assignees, … */ }

// Application.onCreate (debug)
DebugTool.install(
    application = this,
    config = DebugToolConfig(
        host = host,
        azure = AzureDevOpsConfig(
            organization = "your-org",
            project = "your-project",
            areaPath = "your-project",
            patProvider = AzurePatProvider { System.getenv("AZURE_DEVOPS_PAT").orEmpty() },
        ),
        azureLabel = "your-project/debugTool",
    ),
)
```

### 3. Wrap UI with `DebugToolScaffold`

```kotlin
DebugToolScaffold {
    AppContent()
}
```

Always-visible draggable FAB + menus/dialogs. Optional `revealMode` reserved for shake-to-reveal later.

### 4. Wire OkHttp

```kotlin
OkHttpClient.Builder()
    .addDebugToolInterceptors(context) // DebugToolNetwork extension
    .build()
```

### 5. Read runtime hooks (any module / release-safe)

```kotlin
val scale = rememberDebugDynamicTypeScale()
val duration = DebugToolHooks.navAnimationDurationMillis(baseMs = 300)
val mockGps = DebugToolHooks.mockLocation()
```

Nav animation duration is exposed **via hooks only** in this iteration (`DebugToolHooks.navAnimationDurationMillis(base)`).

---

## Migration from v1.0.x

| Before (1.0.x) | After (1.1.0) |
|----------------|---------------|
| `debugImplementation("…:debugtool:…")` only | Add `implementation("…:debugtool-hooks:1.1.0")` for all variants |
| Copy FAB + dialogs from sample `MainActivity` | Use `DebugToolScaffold { … }` |
| `DebugNetworkRegistry.contributor.contribute(…)` | `builder.addDebugToolInterceptors(context)` / `DebugToolNetwork.interceptor(context)` |
| `DebugRuntimeRegistry.get()` | `DebugToolHooks` (typography, mock GPS, halt, nav duration, …) |
| Host/Config in `:debugtool` | Host/Config live in `:debugtool-hooks` (`com.quadlogixs.debugtool.api`) |

`DebugRuntimeRegistry` and `DebugNetworkRegistry` remain **deprecated but functional** after `DebugTool.install`.

---

## Try the sample app

1. Open this project in Android Studio  
2. Run `:app` (debug)  
3. Tap the floating bug icon  

Release compile of `:app` uses hooks-only (no full menu).

---

## Project layout

| Module | Purpose |
|--------|---------|
| `:debugtool-hooks` | Always-safe Host/Config + `DebugToolHooks` + `DebugToolNetwork` |
| `:debugtool` | Full debug UI, Chucker, Azure, install (`api` → hooks) |
| `:app` | Sample host |

---

## Rules

| Do | Don’t |
|----|--------|
| `implementation(hooks)` + `debugImplementation(debugtool)` | Ship full `debugtool` in release |
| Use `DebugToolScaffold` | Re-copy FAB wiring from old samples |
| `addDebugToolInterceptors` | Expect Chucker/mock/halt without OkHttp wiring |
| Set `AZURE_DEVOPS_PAT` for bug reports | Hardcode PATs |

---

## Publish (maintainers)

```bash
./gradlew :debugtool-hooks:publishToMavenLocal :debugtool:publishToMavenLocal
```

Tag `1.1.0` on GitHub → wait for JitPack green → consume as above.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Dependency not found | Wait for JitPack, or `mavenLocal()` after publishToMavenLocal |
| No bug icon | Debug build + `DebugToolScaffold` + `DebugTool.install` |
| Network tools missing | Call `addDebugToolInterceptors` |
| Dynamic type unused | Read `DebugToolHooks.typographyScale` / `rememberDebugDynamicTypeScale()` |
| Bug report fails | Set `AZURE_DEVOPS_PAT` + valid Azure config |
