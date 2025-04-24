# Indhan Mitra 🚗⛽

**Indhan Mitra** (meaning "Fuel Friend" in Hindi) is an Android application designed to help users easily locate and navigate to nearby fuel stations across India.

## 🌟 Key Features

- **Nearby Fuel Stations**: Find stations within a 5km radius
- **Comprehensive Station Information**:
  - Distance to station
  - Fuel types available (Petrol, Diesel, CNG, EV Charging)
  - Current fuel prices
  - Traffic conditions and estimated travel time
- **Real-time Navigation**: Direct navigation to selected stations
- **Multi-fuel Support**: Locate different types of fuel stations in one app

## 🛠 Project Configuration

### Android Specifications
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Compile SDK**: 34
- **Version**: 1.0 (Version Code: 1)

### Build Details
- **Build Tools Version**: 8.1.0
- **Kotlin Version**: 2.0.0
- **Compose Compiler Extension Version**: 1.4.0
- **Java Compatibility**: Java 1.8

## 📦 Dependencies

### Core Dependencies
```groovy
dependencies {
    // Kotlin
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.0"
    
    // AndroidX Core
    implementation 'androidx.core:core-ktx:1.9.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.12.0'
    
    // Jetpack Compose
    implementation platform('androidx.compose:compose-bom:2023.01.00')
    implementation 'androidx.compose.ui:ui:1.4.0'
    implementation 'androidx.compose.material3:material3:1.0.0'
    
    // Navigation and Lifecycle
    implementation 'androidx.navigation:navigation-compose:2.5.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.0'
    
    // Maps and Location
    implementation 'com.google.maps.android:maps-compose:2.7.2'
    implementation 'com.google.android.gms:play-services-maps:18.0.2'
    implementation 'com.google.android.gms:play-services-location:21.3.0'
    
    // Additional Libraries
    implementation 'com.google.android.libraries.places:places:2.5.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.1'
    implementation 'com.airbnb.android:lottie-compose:5.2.0'
    
    // Permissions and Security
    implementation 'com.google.accompanist:accompanist-permissions:0.28.0'
    implementation 'androidx.security:security-crypto:1.1.0-alpha03'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
}
```

### Testing Dependencies
```groovy
dependencies {
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4:1.4.0'
}
```

## 🚀 Getting Started

### Prerequisites

- Android 7.0 (Nougat) or higher
- Google Play Services
- Active internet connection

### Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/indhan-mitra.git
```

2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run the application

## 🔐 Authentication

The app provides a simple authentication system with:
- User registration
- Login functionality
- "Remember Me" option
- Local SQLite database for user management

## 🗺 How It Works

1. Launch the app
2. Sign up or log in
3. Grant location permissions
4. View nearby fuel stations on the map
5. Tap a station to see detailed information
6. Use "Get Directions" to navigate

## 🎨 UI Features

- Color-coded station markers
- Responsive design with Jetpack Compose
- Smooth animations
- Expandable/collapsible information panels

## Screenshot
![fuelstats](https://github.com/user-attachments/assets/882d63b8-b2c9-404b-9983-99c715e773a2)


## 🚧 Upcoming Improvements

- Enhanced security measures
- Offline mode support
- User profile management
- Fuel price tracking and alerts
- Advanced filtering options

## 🔒 Security Considerations

- Implement secure password hashing
- Move hardcoded API keys to secure locations
- Enhance SharedPreferences security

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting a pull request.

## 📞 Support

For issues or feature requests, please open an issue on our GitHub repository.

---

**Made with ❤️ for Indian Drivers**
