import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Switch } from 'react-native';
// import BackgroundLocation from 'react-native-background-location';

export default function App() {
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    // BackgroundLocation.configure({
    //   desiredAccuracy: 'HIGH',
    //   distanceFilter: 99,
    //   stopTimeout: 10,
    //   stopOnTerminate: false,
    //   startOnBoot: false,
    //   notificationTitle: 'This is custom title',
    //   notificationDescription: 'custom description for',
    // });
    // Subscribe to location change events
    // const locationSubscription = BackgroundLocation.onLocation((data) => {
    //   console.log('Location changed:', data);
    // });
    // const onMotionChangeSubscription = BackgroundLocation.onMotionChange(
    //   (data) => {
    //     console.log('motion changed:', data);
    //   }
    // );
    // const activityChangeSubscription = BackgroundLocation.onActivityChange(
    //   (data) => {
    //     console.log('onActivityChange:', data);
    //   }
    // );
    // const ProviderSubscription = BackgroundLocation.onProviderChange((data) => {
    //   console.log('onProviderChange:', data);
    // });
    // if (enabled) {
    //   BackgroundLocation.start().then((res) =>
    //     console.log('Background location started', res)
    //   );
    // } else {
    //   BackgroundLocation.stop().then(() =>
    //     console.log('Background location stopped')
    //   );
    // }
    // return () => {
    //   locationSubscription.remove(); // Unsubscribe from the event
    //   onMotionChangeSubscription.remove(); // Unsubscribe from the event
    //   activityChangeSubscription.remove(); // Unsubscribe from the event
    //   ProviderSubscription.remove(); // Unsubscribe from the event
    // };
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
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
