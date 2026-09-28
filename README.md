# Hybrid V3.6 Signal Monitor (ETHUSDT 4H) - monitor only, no trading

Kotlin, minSdk 23 / targetSdk 35 / compileSdk 35, AGP 8.7.3, Gradle 8.9, JDK 17+.
Dependencies: androidx.core, androidx.work only. No native code (runs natively on arm64-v8a).
Binance public klines only (data-api.binance.vision first, then api*.binance.com). No keys, no orders.

## Get the APK
1. Android Studio: open this folder, let it sync, Build > Build APK(s)  (or `gradle assembleDebug`).
   Output: app/build/outputs/apk/debug/app-debug.apk (debug-signed, directly installable).
2. No local setup: push this folder to a GitHub repo; the included workflow
   (.github/workflows/build-apk.yml) builds and uploads the APK as an artifact.
3. Then run ./verify_apk.sh (manifest, dex, resources, signature, SDK levels, launcher activity).

## Design notes
- Only CLOSED candles feed the strategy (closed = closeTime < Binance server time at fetch).
- Candles are cached in SQLite and accumulated, so the replay always starts from the same first bar.
- Signals: SQLite table `signals`, unique on (candle_time, type). Notification flag is flipped before
  posting -> at most one notification per signal. Signals older than 8 h are stored silently.
- Background: WorkManager, 15-min periodic, network-constrained, skips the network if the latest
  closed candle is already cached. Doze/battery limits can delay it.

## Interpretation choices (spec was ambiguous - check against your Pine script)
- Entry price = close of the signal candle; exits are evaluated on later closed bars using high/low.
  If stop and target hit in the same bar, stop wins. Gap through a stop fills at the open.
- Pivot = strictly higher/lower than all 3 bars on each side. crossover = Pine ta.crossover semantics.
- "Range candidate" = original range condition. "Near lower/upper edge" = close within rangeEdgeZone (0.35).
- Break-even and trailing apply to trend trades only; range trades use fixed stop and rangeMid target.
- No fees, slippage, leverage cap or reverse-signal exits (none were specified).
- Indicators are SMA-seeded (Pine style); expect tiny differences vs TradingView on early history.
