import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-background-location' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const BackgroundLocation = NativeModules.BackgroundLocation
  ? NativeModules.BackgroundLocation
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function saveConfiguration(config: Object): Promise<string> {
  return BackgroundLocation.saveConfiguration(config);
}
export function getConfiguration(config: Object): Promise<string> {
  return BackgroundLocation.getConfiguration(config);
}

export function startBackgroundLocation(): Promise<string> {
  return BackgroundLocation.startBackgroundService();
}

export function stopBackgroundLocation(): Promise<string> {
  return BackgroundLocation.stopBackgroundService();
}
