# Changelog

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
