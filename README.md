# WatchPatrol — native Android client

A modern, native rebuild of the WatchPatrol experience in **Kotlin + Jetpack
Compose**. The APK floating around as the "official" app is a Chrome-minted
**WebAPK** — a PWA bookmark whose home screen is hard-wired to a single
listing page. This is a real app instead: it talks directly to the WatchPatrol
REST API, so it's fast, native, and actually navigable.

> Personal, unofficial client for watchpatrol.net. Not affiliated with
> Sixty Clicks Media. The launcher icon is an original mark, not their logo.

## What's in it

- **Discover** — infinite, cursor-paginated feed of the newest listings across
  Reddit + the forums, with price, source and age at a glance.
- **Search** — debounced free-text search over the whole index.
- **Listing detail** — full image, price, seller, source, star-to-save, and a
  one-tap jump to the original post.
- **Browse** — the brand catalog as a grid → a brand's models → **model market
  page** with median price, 25th/75th percentiles, sample count, a
  distribution **histogram**, a price-**history chart**, and that model's live
  listings.
- **Identify** — pick a photo and get the closest-matching models with
  confidence scores and median prices.
- **Starred** — listings you save, persisted locally (works offline).

## Architecture

Single module, deliberately boring and dependency-light so it builds anywhere
with **no annotation processors** (no Hilt, no Room, no kapt/ksp beyond the
standard Kotlin compiler plugins).

```
ui/            Compose screens + ViewModels (MVVM), one package per feature
  components/  shared UI: ListingCard, charts, states, search bar, top bar
  theme/       Material 3 dark-first theme
  navigation/  NavHost + bottom bar
domain/model/  app-facing models + DTO→domain mappers
data/
  remote/      Retrofit API, ApiConfig, DTOs (the wire format)
  repository/  ListingsRepository, CatalogRepository, PricingRepository, IdentifyRepository
  paging/      generic cursor PagingSource
  local/       DataStore-backed StarredStore
di/            AppContainer (hand-rolled DI) + wpViewModel() helper
```

- **Networking:** Retrofit + OkHttp + kotlinx.serialization (lenient parser)
- **Paging:** Paging 3 (`CursorPagingSource` follows the API's `next` URL)
- **Images:** Coil
- **Local store:** DataStore Preferences
- **State:** `StateFlow` + `collectAsStateWithLifecycle`

## Build

Open in **Android Studio** (Koala or newer) and let it sync — it will fetch the
Gradle wrapper and dependencies. Or from the CLI once a wrapper jar exists:

```
./gradlew :app:assembleDebug
```

Config: `compileSdk/targetSdk 35`, `minSdk 28`, JDK 17, Kotlin 2.1, AGP 8.7.
Version numbers live in `gradle/libs.versions.toml` — bump freely and re-sync.

> Note: the binary `gradle/wrapper/gradle-wrapper.jar` is **not** included
> (can't ship a binary here). Android Studio regenerates it on open, or run
> `gradle wrapper --gradle-version 8.11.1` once if you have Gradle installed.

## ⚠️ Wiring the live API (read this first)

I built this against WatchPatrol's **documented** REST shape
(watchpatrol.net/integrations: brands/models catalog, pricing with
median/quartiles/histogram + bulk-history, a deduped listings feed with
cursor pagination and `?q=`, and photo identify). I could **not** reach the
live API from the build environment, so the exact **paths and JSON field
names are best-guesses** (DRF snake_case). Everything is isolated so this is a
quick fix, not a rewrite:

| What to confirm | File |
|---|---|
| Base URL, API key / auth header | `data/remote/ApiConfig.kt` |
| Endpoint paths + query param names | `data/remote/WatchPatrolApi.kt` |
| JSON field names (`@SerialName`) | `data/remote/dto/Dtos.kt` |
| Identify multipart field name (`"image"`) | `data/repository/IdentifyRepository.kt` |

Nothing else depends on the wire format — every screen consumes the mapped
`domain/model` types, so correcting the DTOs/paths flows through automatically.

**Fastest way to verify:** grab the OpenAPI schema linked on the integrations
page (look for an `openapi.json` / schema link), or just run the debug build —
`AppContainer` installs an OkHttp logging interceptor, so Logcat shows each
request URL and response. Adjust `@SerialName`/paths until the feed populates.

Robustness already built in: the JSON parser is lenient and ignores unknown
keys, prices are read whether sent as JSON numbers or strings
(`FlexibleDoubleSerializer`), and missing fields degrade gracefully instead of
crashing. So even partially-wrong field names won't take the app down — you'll
just see blanks where a name needs fixing.

### Assumptions to check
- Base URL `https://www.watchpatrol.net/api/` and trailing-slash paths
  (`listings/`, `brands/`, `models/`, `pricing/{slug}/`,
  `pricing/bulk-history/`, `identify/`).
- Listing id is numeric (`/listing/<id>/`). If ids are slugs, change
  `ListingDto.id` and the `listing/{id}` nav arg to `StringType`.
- Cursor pagination returns `{ results, next, previous, count }` with `next`
  as an absolute URL.
- Listings are public (no key). If not, set `ApiConfig.API_KEY` + the header
  scheme in `apiKeyValue()`.

Happy to wire it precisely the moment you can share the OpenAPI schema.
