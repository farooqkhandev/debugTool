# Changelog

## 1.0.5

- Professional dark-mode UI redesign (`DebugToolTheme` / `DebugColors`) across FAB, menu, and feature screens.
- Shared components (`CardContainer`, `BaseButton`, `TextInputFieldApp`) use dark debug palette instead of host light theme.
- Accent-coded feature screens (teal / purple / green / orange / blue / red) aligned to mockups.

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
