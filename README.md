# 🌍 react-native-geo-locator

[![npm version](https://img.shields.io/npm/v/react-native-geo-locator.svg)](https://www.npmjs.com/package/react-native-geo-locator)
[![license](https://img.shields.io/github/license/yourusername/react-native-geo-locator.svg)](https://github.com/yourusername/react-native-geo-locator/blob/main/LICENSE)

`react-native-geo-locator` is a powerful, lightweight, and customizable library for tracking user location in the background in React Native applications. It enables real-time geolocation updates even when the app is running in the background, making it perfect for a variety of location-based services.

## 🚀 Features

- 📍 Track user location in the background
- 🏃‍♂️ Detect motion and activity changes
- 🔄 Listen to changes in location provider (e.g., GPS, network)
- ⚙️ Highly configurable with options for desired accuracy, distance filtering, and more
- 🔋 Battery-optimized for efficient power consumption
- 🪶 Lightweight implementation for minimal app size impact
- 🤖 Android support (iOS support coming soon!)

## 📦 Installation

```bash
npm install react-native-geo-locator
# or
yarn add react-native-geo-locator
```

## 🛠 Setup

### Android

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
```

### iOS (Coming Soon)

iOS support is planned for future updates. Stay tuned!

## 🔧 Usage

Here's a quick example to get you started:

```jsx
import React, { useState, useEffect } from 'react';
import { View, Text, Switch } from 'react-native';
import BackgroundLocation from 'react-native-geo-locator';

export default function App() {
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    BackgroundLocation.configure({
      desiredAccuracy: 'HIGH',
      distanceFilter: 100,
      notificationTitle: 'Location Tracking',
      notificationDescription: 'Your location is being tracked.',
    });

    const locationSubscription = BackgroundLocation.onLocation((data) => {
      console.log('Location changed:', data);
    });

    if (enabled) {
      BackgroundLocation.start();
    } else {
      BackgroundLocation.stop();
    }

    return () => locationSubscription.remove();
  }, [enabled]);

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Text>Enable Background Location</Text>
      <Switch value={enabled} onValueChange={setEnabled} />
    </View>
  );
}
```

## 📚 API Reference

### `BackgroundLocation.configure(options)`

Configures the background location settings.

| Option                    | Type    | Description                             | Default                  |
| ------------------------- | ------- | --------------------------------------- | ------------------------ |
| `desiredAccuracy`         | String  | Accuracy level: 'HIGH', 'MEDIUM', 'LOW' | 'LOW'                    |
| `distanceFilter`          | Number  | Minimum distance (meters) for updates   | 50                       |
| `stopTimeout`             | Number  | Time (minutes) to stop if stationary    | 5                        |
| `stopOnTerminate`         | Boolean | Stop tracking on app termination        | true                     |
| `startOnBoot`             | Boolean | Start tracking on device boot           | false                    |
| `notificationTitle`       | String  | Notification title                      | 'App is running'         |
| `notificationDescription` | String  | Notification description                | 'Tracking your location' |

### Event Listeners

- `BackgroundLocation.onLocation(callback)`
- `BackgroundLocation.onMotionChange(callback)`
- `BackgroundLocation.onActivityChange(callback)`
- `BackgroundLocation.onProviderChange(callback)`

### Control Methods

- `BackgroundLocation.start()`
- `BackgroundLocation.stop()`

## 🔋 Battery Optimization

`react-native-geo-locator` is designed with battery efficiency in mind. It uses intelligent algorithms to minimize battery drain while still providing accurate location updates. The library:

- Adapts tracking frequency based on movement detection
- Uses low-power location providers when high accuracy is not required
- Implements efficient background processing to reduce CPU usage

## 🪶 Lightweight Implementation

Despite its powerful features, `react-native-geo-locator` maintains a small footprint:

- Minimal impact on app size
- Efficient memory usage
- Quick initialization and low overhead

These characteristics make it an excellent choice for developers who need robust location tracking without sacrificing app performance.

## 🔜 Roadmap

- [ ] iOS support
- [ ] Geofencing capabilities
- [ ] Enhanced battery optimization strategies
- [ ] More customizable notification options

## 🤝 Contributing

We welcome contributions! Please check out our [contributing guide](CONTRIBUTING.md) to learn more about how to get involved.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgements

- [create-react-native-library](https://github.com/callstack/react-native-builder-bob) for the initial setup
- All our contributors and users!

---

Made with ❤️ by vishal
