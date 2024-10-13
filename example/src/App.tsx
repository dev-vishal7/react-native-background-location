import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Switch } from 'react-native';
import {
  startBackgroundLocation,
  stopBackgroundLocation,
  saveConfiguration,
} from 'react-native-background-location';

export default function App() {
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    saveConfiguration({
      desiredAccuracy: 'HIGH',
      distanceFilter: 99,
      stopTimeout: 5,
      stopOnTerminate: false,
      startOnBoot: false,
      notificationTitle: 'App is running on background',
      notificationDescription: 'Tracking your location',
    })
      .then((res) => {
        console.log('res----', res);
      })
      .catch((err) => {
        console.log('err', err);
      });
    if (enabled) {
      startBackgroundLocation().then((res) =>
        console.log('Background location started', res)
      );
    } else {
      stopBackgroundLocation().then(() =>
        console.log('Background location stopped')
      );
    }
  }, [enabled]);

  return (
    <View style={styles.container}>
      <View style={{ alignItems: 'center' }}>
        <Text>Click to enable Background Geolocation Changed</Text>
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
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
