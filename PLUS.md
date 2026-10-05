# Immich TV Plus

Fork von [giejay/Immich-Android-TV](https://github.com/giejay/Immich-Android-TV) mit **KI-Suche**, **Favoriten**, **Orten** und einem **In-App-Updater**. Die Originalfunktionen bleiben unverändert.

**Installation auf dem TV** (App „Downloader“):

```
https://github.com/thimo513/Immich-Android-TV/releases/latest/download/ImmichTVPlus.apk
```

Anmeldung über „Sign in“ mit Server-URL und API-Key. Demo-Modus und Handy-Login nutzen giejays private Dienste und funktionieren im Fork nicht.

## Aufbau

| Pfad | Inhalt |
| --- | --- |
| `app/src/plus/` | Gesamter neuer Code, Ressourcen und Manifest-Ergänzungen |
| `app/plus.gradle` | App-Kennung `nl.giejay.android.tv.immich.plus`, Versionierung, Source-Sets, Firebase-Platzhalter |
| `.github/workflows/plus-sync.yml` | Täglicher Abgleich mit dem Original, Build, Release |

Eingriffe in Originaldateien (bewusst minimal, damit Merges konfliktfrei bleiben):

1. `app/build.gradle`: `apply from: 'plus.gradle'`
2. `HomeFragment.kt`: eine Zeile `*PlusMenu.headers()` in `HEADERS`
3. `nav_graph.xml`: Destination `plusPlaceAssetsFragment`
4. `VerticalCardGridFragment.kt`: `VerticalGridPresenter(ZOOM_FACTOR, false)`, schaltet das Abdunkeln nicht fokussierter Vorschaubilder aus (Leanback dimmt sie standardmäßig)

## Branches und Automatik

- `main`: exakte Kopie des Originals
- `plus`: `main` plus unsere Änderungen, Standardbranch, Quelle der Releases

Der Workflow merged täglich `upstream/main` in `plus`, baut eine signierte APK und veröffentlicht sie als Release `plus-<versionCode>`. Bei einem Konflikt wird nichts veröffentlicht, stattdessen entsteht ein Issue „Sync-Konflikt mit dem Original“.

Versionierung: `versionCode = Original × 1000 + Buildnummer`, `versionName = <Original>-plus.<Buildnummer>`.

## Lokal bauen

```bash
./gradlew assembleDebug
```

Voraussetzungen: JDK 17, Android SDK mit Plattform 37. `google-services.json` und `strings_other.xml` erzeugt `plus.gradle` automatisch aus den Beispieldateien.
