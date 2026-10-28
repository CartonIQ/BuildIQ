BuildIQ - build notes (Termux ARM)
1. pkg install openjdk-17 gradle aapt2
2. Create local.properties with:  sdk.dir=/path/to/android-sdk   (platforms;android-34, build-tools;34.0.0)
3. In gradle.properties uncomment android.aapt2FromMavenOverride (path to Termux aapt2)
4. gradle wrapper --gradle-version 8.7     (generates gradlew + wrapper jar)
5. ./gradlew assembleDebug
Before publishing: swap AdMob test IDs for real ones, and set privacy_url in res/values/strings.xml.
