# BuildIQ - Professional Construction Calculator & Cost Estimator Suite

BuildIQ is an industry-grade Android application engineered for general contractors, remodelers, tradespeople, estimators, and DIY builders.

---

## 🛠 Features & Capabilities

### 1. 18 Comprehensive Construction Calculators
- **Paint Calculator**: Computes gross/net wall areas with custom door (20 sq ft / 1.86 m²) and window (15 sq ft / 1.39 m²) deductions, multiple coats, configurable waste %, and coverage rates.
- **Primer / Sealer**: Dedicated primer estimation for drywall prep, stain blocking, and color transitions.
- **Ceiling Paint & Texture**: Flat, tray, and popcorn textured ceilings with specific texture absorption factors.
- **Doors & Windows (Trim & Deductions)**: Complete casing trim lineal lengths (8ft / 2.4m sticks), miter cut allowances, and net wall opening deductions.
- **Flooring (Hardwood / Laminate / Vinyl Plank)**: Room square footage, box coverage conversion, expansion gaps, and 5-15% cut waste.
- **Floor & Wall Tile**: Exact tile counts based on custom tile dimensions (e.g. 12x12, 12x24, 6x24), straight/diagonal patterns, and carton packaging.
- **Tile Grout**: Accurate grout weight (lbs or kg) computed from tile length, width, thickness, and joint gap width (e.g., 1/8" or 3mm).
- **Drywall & Supplies**: Panel requirements (4x8 ft or 4x12 ft / 1.2x2.4m or 1.2x3.6m), ceiling toggles, joint compound gallons/liters, screws, and seam tape rolls.
- **Roofing (Shingles & Underlayment)**: Slope pitch multipliers (rise/12), eave overhangs, roofing squares, 3-bundle per square calculations, and felt underlayment rolls.
- **Fencing**: Total fence length, post spacing (6-8 ft / 2.4m), post counts, 2/3 horizontal rails, vertical pickets, and quick-set concrete bags.
- **Decking**: Deck boards linear footage, board gaps, joist counts at 16" on center, and fastener screw counts.
- **Mulch**: Garden bed area, depth in/cm, bulk cubic yards / cubic meters, and 2 cu ft / 56L bags.
- **Gravel & Crushed Stone**: Driveways and pathways, depth, compaction factor, volume, and tonnage (1.4 tons/yd³).
- **Soil & Topsoil**: Raised beds and lawn topdressing, cubic volume, and 1 cu ft / 28L bags.
- **Concrete Slab & Footings**: Slab dimensions, depth, cubic volume, and 80 lb / 60 lb / 25 kg pre-mix bag counts.
- **Area & Dimensions**: Rapid perimeter and unit conversions between sq ft, sq yd, and sq meters.
- **Wallpaper**: Double roll coverage (56 sq ft) with 15-20% pattern repeat allowances.
- **Lumber & Board Feet**: Nominal thickness, width, length, quantity, and total board feet (BF).

### 2. Dual Unit System (Imperial & Metric)
- Seamless one-tap toggle between **Imperial** (ft, in, sq ft, cu yd, gal, lbs) and **Metric** (m, cm, sq m, cu m, liters, kg).
- Persistent unit preferences configurable in Settings.

### 3. Integrated Pricing Engine
- **Itemized Material Catalog**: Editable prices for all materials with custom category groupings.
- **Custom Materials**: Add custom products with user-defined units (per sq ft, per bag, each, etc.).
- **Labor Calculator**: Supports Flat Rate or Hourly Rate (rate × hours).
- **Contingency Factor**: Configurable risk allowance (e.g. 5-10%).
- **Markup / Profit Margin**: Configurable contractor markup (e.g. 10-25%).
- **Sales Tax**: Configurable local sales tax rate.
- **Configurable Currency**: Symbol customizable ($, €, £, CA$, AU$, ₹, etc.).

### 4. Multi-Item Projects & Combinations
- Combine multiple trade calculations (e.g. Living Room Paint + Flooring + Trim) into a unified project estimate.
- Reopen, edit, duplicate, rename, and delete projects.
- Safe SQLite / Room Database migration (`MIGRATION_1_2`) ensuring zero data loss when upgrading from v1.

### 5. Professional Quotation Generation
- **PDF Export**: Generates clean vector PDF estimates using Android's native `PdfDocument` engine with company branding, contractor license, client site details, itemized tables, financial summaries, and client acceptance signature lines.
- **Text Estimates**: Formatted plain text summaries ready for instant sharing via SMS, WhatsApp, or email through Android's native Share Sheet.

### 6. Backup & Restore
- Complete JSON data export of all projects, custom prices, and settings.
- Robust JSON import with schema validation and options to **Merge** or **Replace All**.

### 7. Google Ads & Privacy Compliance
- Google Mobile Ads with test AdMob IDs in development.
- Google User Messaging Platform (UMP) consent SDK implementation for GDPR / privacy compliance.
- R8 / ProGuard rules configured for minification and shrink resources in release.

---

## 🚀 Building the Project

### Requirements:
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 (Java 17)
- Android SDK 34 (Android 14)

### Opening in Android Studio:
1. Open Android Studio.
2. Select **Open** and choose the `BuildIQ` root folder.
3. Allow Gradle to sync dependencies.
4. Select `app` run configuration and click **Run** (or Shift+F10).

### Command-Line Build:
```bash
# Run unit tests
./gradlew test

# Assemble Debug APK
./gradlew assembleDebug
# Generated APK will be located at: app/build/outputs/apk/debug/app-debug.apk

# Assemble Release APK (R8 minified)
./gradlew assembleRelease
```

### Release Signing:
To sign release builds securely without hardcoding credentials in Git:
Set the following environment variables:
```bash
export KEYSTORE_PATH="/path/to/keystore.jks"
export KEYSTORE_PASSWORD="your-keystore-password"
export KEY_ALIAS="your-key-alias"
export KEY_PASSWORD="your-key-password"
./gradlew assembleRelease
```

---

## 🧪 Unit Tests
The project contains comprehensive unit tests in `app/src/test/java/com/cartoniq/buildiq/CalculatorTest.kt` verifying:
- Unit conversion accuracy (Imperial <-> Metric)
- All calculator formulas, rounding, and waste calculations
- Pricing engine financial calculations (Labor, Tax, Contingency, Markup, Grand Total)
- JSON serialization and deserialization of multi-calculation project items
