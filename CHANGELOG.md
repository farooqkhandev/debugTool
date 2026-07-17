# Changelog

## 1.0.6

- **Theme isolation:** host app UI is never wrapped in the library MaterialTheme (fixes teal/green overriding host primary, e.g. yellow Login button). `DebugTheme` applies only to debug menu/dialogs.
- **Network Trace:** menu action opens Chucker (no more `Action: NetworkTrace` toast).
- Dynamic Type density override applies only when scale ≠ 1.0.
- Prefer this tag over GitHub/`JitPack` **1.0.5**, which pointed at the dark redesign that wrapped the entire scaffold (including host content) in teal `DebugToolTheme`.

## 1.0.5

- Superseded — do not use. Tag pointed at an incomplete dark-theme redesign that leaked library colors into host apps.

## 1.0.4

- `:debugtool` depends on hooks via `compileOnly` (not `api`) so the published POM does **not** pull hooks transitively.
- Hosts must declare both artifacts explicitly — no `exclude { }` needed.
- Version badge / docs updated for dual-artifact install.

## 1.0.3

- New always-safe module `:debugtool-hooks` (`DebugToolHooks`, `DebugToolNetwork`, Host/Config APIs).
- Full `:debugtool` depends on hooks via `api`; install wires both new hooks and deprecated registries.
- `DebugToolScaffold` — always-visible draggable FAB + menu/dialogs (optional `revealMode`).
- `DebugTool.createShakeDetector` + `ShakeHandle` wrap existing shake detection.
- OkHttp: `DebugToolNetwork.addDebugToolInterceptors` / `interceptor` (no-op without full lib).
- Deprecate `DebugRuntimeRegistry` / `DebugNetworkRegistry` (still functional).
- Sample app: debug uses Scaffold; release compiles against hooks-only.
- Nav animation duration via `DebugToolHooks.navAnimationDurationMillis(base)` only this iteration.

## 1.0.2

- Prior standalone debug menu library release.

## 1.0.1

- Real CameraX + ML Kit QR scanner (replaces camera preview stub).

## 1.0.0

- Initial JitPack release.
