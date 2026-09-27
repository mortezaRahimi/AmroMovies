# AmroMovies

## Project Overview

AmroMovies is an Android application that displays TMDB's weekly trending movies, limited to the first 100 unique results in TMDB trending order. Users can filter that dataset by genre, sort it by popularity, title, or release date, refresh it, and open a movie-detail screen.

The implemented assignment scope includes:

- a Compose and Material 3 UI for trending movies and movie details;
- local genre filtering and ascending/descending sorting;
- TMDB weekly trending, movie genres, and movie-detail API integration;
- Room persistence for the ranked weekly trending dataset;
- MVVM with unidirectional actions and immutable `StateFlow` UI state;
- Koin dependency injection; and
- focused repository, network, mapping, ViewModel, Room, and Compose tests.

## Requirements

- Android Studio with support for Android Gradle Plugin 9.2.1 and Kotlin 2.2.10.
- JDK 21 for the Gradle daemon. The repository declares this in `gradle/gradle-daemon-jvm.properties` and uses the Foojay resolver convention for toolchain resolution.
- Android SDK Platform 36. The application compiles against Android 36.1, targets API 36, and supports API 24 and newer.
- No separate Gradle installation is required. The included wrapper uses Gradle 9.4.1.
- A TMDB API Read Access Token.

## Setup

1. Clone the repository and enter its directory:

   ```bash
   git clone <repository-url>
   cd AmroMovies
   ```

2. Open the project in Android Studio and allow it to install or select the required JDK and Android SDK.

3. Add a TMDB API Read Access Token using one of the methods in the next section.

4. Sync the Gradle project.

5. Select the `app` run configuration and run it on an emulator or device running Android 7.0/API 24 or newer.

## TMDB Credential Configuration

The build requires a TMDB API Read Access Token. The token is sent as a Bearer token in the `Authorization` header; it is not expected to be a TMDB v3 API key.

For local development, add this entry to the root `local.properties` file:

```properties
access_token=YOUR_TMDB_API_READ_ACCESS_TOKEN
```

Alternatively, define an environment variable before invoking Gradle:

```bash
export TMDB_ACCESS_TOKEN=YOUR_TMDB_API_READ_ACCESS_TOKEN
```

`TMDB_ACCESS_TOKEN` takes precedence over `local.properties`. Gradle escapes the configured value and exposes it to the application as `BuildConfig.TMDB_ACCESS_TOKEN`. Koin supplies that value when constructing the network client.

`local.properties` is excluded by the repository's `.gitignore`. Never commit a real credential or place one in a tracked Gradle file. As with any credential embedded in a client application, the resulting APK cannot provide server-grade secret storage; a backend proxy would be required if that threat model becomes relevant.

## How to Build and Run

All commands run from the repository root and require the credential configuration above, including test and lint tasks because the application module validates the token during Gradle configuration.

```bash
# Build the debug APK
./gradlew assembleDebug

# Run all local unit and Robolectric tests
./gradlew test

# Run the debug unit-test variants
./gradlew testDebugUnitTest

# Run Android lint for debug variants
./gradlew lintDebug

# Build the minified and resource-shrunk release APK
./gradlew assembleRelease
```

Run the app itself from Android Studio, or install the generated debug APK on a connected device with the usual Android tooling.

## Architecture

The project applies Clean Architecture principles without adding a use-case class for every repository call or introducing generic base classes.

```text
Compose Route / Screen
        ↓ actions
ViewModel (StateFlow)
        ↓
Domain MoviesRepository contract
        ↓
DefaultMoviesRepository
        ├── MoviesRemoteDataSource → Retrofit / TMDB
        └── MoviesLocalDataSource  → Room
        ↓ state
ViewModel → immutable UI state → Compose
```

The domain layer owns application models, typed results/errors, and the repository contract. The data layer implements orchestration, caching policy, transport-to-domain mapping, and technical-error mapping. Network and database modules implement data-source contracts and keep DTOs and Room entities inside their respective modules. Feature modules depend on domain contracts and presentation infrastructure, not Retrofit, Room, DTOs, or entities.

No standalone use-case layer is currently present because the implemented operations do not add semantics beyond the repository contract. A use case should be introduced when it coordinates meaningful business behavior, not simply to add another forwarding class.

## Module Structure

| Module | Responsibility | Direct project dependencies | Why it exists |
|---|---|---|---|
| `:app` | Application entry point, navigation, BuildConfig credential wiring, and Koin composition root | All core and feature modules | Owns Android application integration and connects implementations to contracts. |
| `:core:common` | Injectable clock abstraction and system implementation | None | Keeps time-based cache behavior deterministic in tests without coupling it to Android. |
| `:core:domain` | `Movie`, `MovieDetails`, `Genre`, `AppResult`, `AppError`, and `MoviesRepository` | None of the other project modules | Defines pure Kotlin application contracts and models. |
| `:core:data` | Repository implementation, remote/local data-source contracts, boundary models, mapping, pagination, caching policy, and error translation | `:core:common`, `:core:domain` | Coordinates data without depending on concrete Retrofit or Room implementations. |
| `:core:network` | TMDB API, DTOs, serialization, authentication, OkHttp, Retrofit, and remote data-source implementation | `:core:data` | Isolates TMDB and HTTP-specific concerns. DTOs remain internal. |
| `:core:database` | Room database, DAO, entities, relationships, cache metadata, and local data-source implementation | `:core:data` | Isolates persistence details while preserving trending rank transactionally. |
| `:core:designsystem` | Shared Material 3 theme, colors, and typography | No project modules | Provides reusable visual foundations without owning feature behavior. |
| `:feature:trending` | Trending UI models/state/actions, ViewModel, pure filtering/sorting, Compose UI, and feature Koin module | `:core:domain`, `:core:designsystem` | Encapsulates the trending user flow. |
| `:feature:moviedetail` | Detail UI model/state/action, ViewModel, formatting, Compose UI, and feature Koin module | `:core:domain`, `:core:designsystem` | Encapsulates the movie-detail user flow. |

This modularization separates volatile infrastructure and independently testable features while avoiding one module per class or per architectural layer fragment.

## Dependency Injection

Koin is used because it aligns with the target team's stack, provides a lightweight Kotlin DSL, and is proportionate to the project size. The application module is the composition root and registers separate network, database, repository, trending, and movie-detail modules.

Long-lived infrastructure and the repository use `single`; ViewModels use Koin's `viewModel` registration. There are currently no standalone use cases requiring `factory` bindings.

Koin remains an implementation detail. Domain models, result types, business rules, and repository contracts do not import Koin or Android `Context`. Hilt was not selected because Koin is an explicit project requirement and already meets the dependency-construction needs here; this is a stack-alignment choice rather than a judgment against Hilt.

## State Management

Both features use MVVM and unidirectional data flow:

1. A stateless Compose screen emits a typed action through `onAction`.
2. The route obtains the Koin ViewModel and forwards the action.
3. The ViewModel processes repository results or applies pure local transformations.
4. The ViewModel updates a private `MutableStateFlow`.
5. Only `StateFlow<...UiState>` is exposed publicly.
6. The route observes state with `collectAsStateWithLifecycle()` and passes it back to the screen.

Initial loading and pull/manual refresh are separate state flags. Loading is presentation state and is not embedded in `AppResult` or propagated through a generic resource wrapper across every layer.

## Network Layer

`TmdbNetworkClient` configures Retrofit, OkHttp, and Kotlinx Serialization. Serialization ignores unknown fields, handles explicit nulls conservatively, and coerces compatible input values. The implemented endpoints are:

- `GET trending/movie/week`;
- `GET genre/movie/list`; and
- `GET movie/{movie_id}`.

OkHttp applies the TMDB Bearer credential and JSON accept header. It uses a 10-second connection timeout and 30-second read, write, and total call timeouts. HTTP logging is installed only when `BuildConfig.DEBUG` is true, uses `BASIC` metadata-level logging, and redacts the authorization header. Release builds do not install the logging interceptor.

TMDB DTOs are internal to `:core:network` and map to data-boundary models before leaving that module. They are never exposed to domain or presentation code.

## Top 100 Trending Logic

TMDB's weekly trending response is paginated. The repository fetches the first page, reads the reported page count, and requests additional pages in structured-concurrency batches using `coroutineScope`, `async`, and `awaitAll` until it has up to 100 unique movies or TMDB has no more pages.

Results are merged in page order, duplicate movie IDs are ignored after their first occurrence, and the resulting dataset is capped at 100. Pagination is therefore invisible to the ViewModel.

Genre filtering and sorting operate only on the canonical cached top-100 dataset already observed by the ViewModel. Changing genre, sort criterion, or direction does not trigger a network request. Supported sorting is popularity, case-insensitive title, and release date in ascending or descending order. Missing release dates have deterministic placement.

## Caching / Offline Strategy

Room is the source of truth for weekly trending content. The repository refresh path is:

```text
TMDB refresh → repository mapping/policy → transactional Room replacement
                                             ↓
                                      observed Room Flow
                                             ↓
                                          ViewModel
```

The cache stores movie fields, genres, movie/genre relationships, explicit trending rank, and a refresh timestamp. Replacing the dataset is transactional so movies, relationships, ordering, and metadata change consistently.

An automatic initial refresh is skipped when the cached weekly dataset is less than one hour old. Manual refresh and retry are forced. An injectable `AppClock` makes freshness behavior deterministic in repository tests, and a mutex prevents duplicate concurrent refresh work.

If refresh fails, already cached trending content remains available through the Room flow while the UI can also display the refresh error. Filtering and sorting never invalidate or refresh the cache.

This is partial offline support, not a complete offline synchronization system. Previously cached trending movies can be displayed without a successful refresh, but movie details are fetched remotely and are not persisted. There is no background synchronization, stale-while-revalidate worker, or conflict-resolution framework.

## Error Handling

HTTP, serialization, and connectivity details are contained at infrastructure/data boundaries:

- the network implementation translates `IOException`, HTTP status failures, and invalid serialized responses into remote data failures;
- the repository maps those failures to `AppError` values such as `NoInternet`, `Unauthorized`, `NotFound`, `Server`, and `Unknown`;
- feature-specific mapper functions translate `AppError` into UI errors; and
- ViewModels do not depend on Retrofit, `HttpException`, Room DAOs, or `IOException`.

Both network and repository boundaries rethrow `CancellationException`, allowing structured coroutine cancellation to propagate. ViewModels consume typed `AppResult` values rather than catching broad technical exceptions. Loading and refreshing remain explicit UI-state concerns.

## Compose Stability and Performance

- Domain models contain no Compose annotations or dependencies.
- Presentation models and UI states use `@Immutable` where their properties satisfy the immutability contract.
- Persistent immutable collections are used in UI state and models where useful.
- No blanket `@Stable` annotations are used.
- Trending movie rows use `movie.id` as their stable `LazyColumn` key; genre controls also use stable genre IDs.
- Filtering, sorting, genre extraction, mapping, and display formatting are outside composables.
- Trending transformations are pure, deterministic, and do not mutate the canonical source list.
- Route composables own lifecycle collection and ViewModel access; screen composables remain stateless.

## Navigation

Navigation Compose defines a trending destination and a `movie/{movieId}` detail destination. Only the `Long` movie ID is placed in the navigation route. The full movie object is not serialized into navigation state.

`MovieDetailViewModel` receives the ID through its Koin parameters and requests the required detail data from `MoviesRepository`. This keeps the detail feature independent from whatever list or deep link opened it.

## Testing Strategy

The repository does not publish a measured coverage percentage.

### Unit / Business Logic Tests

- every genre-filter, sort-option, and direction combination;
- case-insensitive title ordering and deterministic missing release dates;
- top-100 filtering boundaries and source-list immutability;
- available-genre derivation;
- domain, cached, remote, and presentation mapping behavior; and
- movie-detail date, currency, runtime, IMDb, and missing-field formatting.

### ViewModel Tests

- initial loading and cached-content success;
- typed failure mapping;
- retry and forced refresh;
- separate refreshing state while content remains visible;
- genre selection without refresh;
- sort selection and direction toggling;
- selected genres with no matching movies; and
- detail loading with the navigation ID, success, error, and retry.

The coroutine tests use `kotlinx-coroutines-test`; Turbine is available where flow assertions benefit from it.

### Repository / Network Tests

- aggregation, ordering, deduplication, exactly 100 results, and fewer-than-100 responses;
- concurrent page completion and concurrent refresh suppression;
- fresh-cache skipping versus forced refresh;
- offline and HTTP failures while preserving cached content;
- cancellation propagation;
- TMDB request paths, authentication, serialization, DTO mapping, and logging configuration; and
- Room trending rank, genre relationships, transactional replacement, and stale-row removal.

Network tests use MockWebServer. Room tests use Robolectric and the Room testing support library.

### Compose Tests

Robolectric Compose tests cover loading, error/retry behavior, canonical and filtered empty states, movie-card content and missing posters, genre selection, combined sort controls, detail fallbacks, and valid IMDb actions. The repository also contains the default Android instrumentation smoke test, but the feature UI tests run as local tests and do not require a device.

## Architectural Decisions

- **Koin instead of Hilt:** required stack alignment with concise constructor-based registrations and no DI dependency in domain code.
- **Moderate modularization:** separate domain, orchestration, infrastructure, design system, and user-facing features without fragmenting every operation into its own module.
- **Domain/UI separation:** domain models describe application data; Compose-aware UI models contain display-ready values and stability contracts.
- **Repository-level error policy:** infrastructure failures become typed application errors before reaching ViewModels.
- **Local filtering and sorting:** the assignment's canonical top 100 is fetched/cached once; presentation choices transform that dataset without changing its membership or causing network traffic.
- **Room as trending source of truth:** observers receive one consistent ordered dataset, and cached content survives refresh failures.
- **No generic `Base*` hierarchy:** there is no `BaseViewModel`, `BaseRepository`, or `BaseUseCase`; shared abstractions exist only where they express a real boundary such as the repository or data-source contracts.

## Trade-offs

- The cache is intentionally limited to weekly trending content rather than introducing a general synchronization engine for the assignment.
- Movie details remain network-only, keeping persistence focused but limiting offline detail access.
- Cache freshness is a fixed one-hour policy rather than server-directed or endpoint-specific invalidation.
- Genre data is fetched as part of a trending refresh and embedded in cached movie relationships; it is not maintained as an independently refreshed catalog.
- Feature modules contain their presentation logic and UI together. Splitting each feature into separate API, domain, data, and UI modules would add build and ownership overhead without a current independent consumer.
- Testing prioritizes repository rules, state transitions, formatting, and important Compose states. It does not attempt exhaustive screenshot, device, performance, or end-to-end coverage.
- The UI focuses on assignment-required content and accessible state handling rather than advanced motion, adaptive multi-pane layouts, or a fully branded component library.

## Known Limitations

- Movie-detail data is not cached, so the detail screen requires a successful network request.
- Trending data has no scheduled/background refresh; refresh occurs when the ViewModel starts with stale data or when the user retries/refreshes.
- TMDB language is currently fixed to `en-US`.
- Filtering supports one genre at a time; there is no search or multi-genre query.
- The app contains no paging UI because pagination is consumed internally to produce the fixed top-100 dataset.
- There are no screenshot/golden tests, performance benchmarks, analytics, crash reporting, or production observability integration.
- The TMDB token is embedded in the built client through `BuildConfig` and can therefore be extracted from an APK; it is protected from source control, not from a determined client-side attacker.

## Future Improvements

- Cache movie details and introduce richer offline synchronization where product requirements justify it.
- Add endpoint-specific cache policies, database migrations as the schema evolves, and scheduled background refresh.
- Add configuration for locale/language and regional content.
- Expand movie information with actors, profiles, trailers, and streaming-provider availability.
- Add search, watchlists, ratings, and other social or account-backed functionality.
- Add analytics, structured logging, crash reporting, and performance observability with an explicit privacy policy.
- Add screenshot testing, device-level navigation tests, benchmarks, and broader accessibility testing.
- Add adaptive layouts for tablets and foldables, plus further keyboard, screen-reader, and large-text validation.
