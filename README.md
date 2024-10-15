# react-native-background-location

`react-native-background-location` is a powerful and customizable library for tracking user location in the background in React Native applications. It enables real-time geolocation updates even when the app is running in the background. This library is suitable for a variety of location-based services such as delivery apps, fitness trackers, ride-sharing platforms, and more.

## Features

- Track user location in the background.
- Detect motion and activity changes.
- Listen to changes in location provider (e.g., GPS, network).
- Highly configurable with options for desired accuracy, distance filtering, and more.
- Android support only (iOS support will be added in future updates).

## Installation

Install the package using npm:

```bash
npm install react-native-background-location
```

Or with yarn:

bash
Copy code
yarn add react-native-background-location
Permissions
To enable background location tracking, you need to add the required permissions.

Android
In your AndroidManifest.xml, add the following permissions:

xml
Copy code
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
iOS (Planned for Future Implementation)
Currently, iOS support is not available. In future updates, you will need to configure the following permissions in your Info.plist file once iOS support is released:

xml
Copy code
<key>NSLocationWhenInUseUsageDescription</key>
<string>We need access to your location to show your current position.</string>
<key>NSLocationAlwaysUsageDescription</key>
<string>We need access to your location even when the app is in the background.</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>We need access to your location to track your movements in the background.</string>
<key>UIBackgroundModes</key>
<array>
<string>location</string>
</array>
Usage Example
Here’s a basic example of how to configure and start background location tracking in your app:

javascript
Copy code
import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Switch } from 'react-native';
import BackgroundLocation from 'react-native-background-location';

export default function App() {
const [enabled, setEnabled] = useState(false);

useEffect(() => {
// Configure background location tracking options
BackgroundLocation.configure({
desiredAccuracy: 'HIGH',
distanceFilter: 99, // Minimum distance (in meters) to trigger location updates
stopTimeout: 10, // Time (in minutes) to stop tracking when stationary
stopOnTerminate: false, // Whether to stop tracking when the app is terminated
startOnBoot: false, // Whether to start tracking on device boot
notificationTitle: 'Location Tracking', // Notification title when tracking is running
notificationDescription: 'Your location is being tracked.', // Notification description
});

    // Subscribe to location change events
    const locationSubscription = BackgroundLocation.onLocation((data) => {
      console.log('Location changed:', data);
    });

    // Subscribe to motion change events
    const onMotionChangeSubscription = BackgroundLocation.onMotionChange((data) => {
      console.log('Motion changed:', data);
    });

    // Subscribe to activity change events (e.g., walking, running)
    const activityChangeSubscription = BackgroundLocation.onActivityChange((data) => {
      console.log('Activity changed:', data);
    });

    // Subscribe to provider change events (e.g., GPS turned off)
    const providerSubscription = BackgroundLocation.onProviderChange((data) => {
      console.log('Provider changed:', data);
    });

    // Start or stop background location tracking based on the toggle switch
    if (enabled) {
      BackgroundLocation.start().then((res) =>
        console.log('Background location started', res)
      );
    } else {
      BackgroundLocation.stop().then(() =>
        console.log('Background location stopped')
      );
    }

    // Clean up subscriptions on unmount
    return () => {
      locationSubscription.remove();
      onMotionChangeSubscription.remove();
      activityChangeSubscription.remove();
      providerSubscription.remove();
    };

}, [enabled]);

return (
<View style={styles.container}>
<View style={{ alignItems: 'center' }}>
<Text>Click to enable Background location</Text>
<Switch value={enabled} onValueChange={setEnabled} />
</View>
</View>
);
}

const styles = StyleSheet.create({
container: {
flex: 1,
alignItems: 'center',
justifyContent: 'center',
},
});
API Reference
BackgroundLocation.configure(options)
Configures the background location settings.

options (Object): An object containing the configuration options:
desiredAccuracy (String): Specifies the desired accuracy level (e.g., 'HIGH').
distanceFilter (Number): Minimum distance (in meters) to trigger a location update.
stopTimeout (Number): Time (in minutes) after which tracking will stop if stationary.
stopOnTerminate (Boolean): Whether to stop location tracking when the app is terminated.
startOnBoot (Boolean): Whether to automatically start location tracking when the device boots.
notificationTitle (String): The title of the notification when location tracking is running.
notificationDescription (String): The description of the notification when location tracking is running.
BackgroundLocation.onLocation(callback)
Subscribes to location change events. The callback function will be called whenever a location update occurs.

BackgroundLocation.onMotionChange(callback)
Subscribes to motion change events. The callback function will be called when the user's motion state changes (e.g., from stationary to moving).

BackgroundLocation.onActivityChange(callback)
Subscribes to activity change events. The callback function will be called when the user's detected activity changes (e.g., walking, running).

BackgroundLocation.onProviderChange(callback)
Subscribes to provider change events. The callback function will be called when the device's location provider (e.g., GPS) changes its state.

BackgroundLocation.start()
Starts background location tracking.

BackgroundLocation.stop()
Stops background location tracking.

iOS Support
Currently, iOS support is not available but will be implemented in future updates. Please stay tuned for updates regarding iOS support.

Contributing
We welcome contributions! Please check out our contributing guide to learn more about how to get involved.

License
MIT License

Made with create-react-native-library
