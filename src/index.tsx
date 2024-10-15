import { NativeModules, DeviceEventEmitter, Platform } from 'react-native';
import type { EmitterSubscription } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-background-location' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const BackgroundLocationNative = NativeModules.BackgroundLocation
  ? NativeModules.BackgroundLocation
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// Create an object that mimics a class-like API
const BackgroundLocation = {
  // Methods to save and get configuration
  configure(config: Object): Promise<string> {
    return BackgroundLocationNative.saveConfiguration(config);
  },

  getConfiguration(): Promise<string> {
    return BackgroundLocationNative.getConfiguration();
  },

  // Start the background location service and listen for location events
  start(): Promise<string> {
    BackgroundLocationNative.startListeningLocationChangeEvent();
    BackgroundLocationNative.startListeningProvideChangeEvent();
    BackgroundLocationNative.startListeningActivityChangeEvent();
    BackgroundLocationNative.startListeningMotionChangeEvent();
    return BackgroundLocationNative.startBackgroundService();
  },

  // Stop the background location service
  stop(): Promise<string> {
    return BackgroundLocationNative.stopBackgroundService();
  },

  // Event subscription methods
  onLocation(callback: (data: any) => void): EmitterSubscription {
    return DeviceEventEmitter.addListener('onLocationChange', callback);
  },

  onProviderChange(callback: (data: any) => void): EmitterSubscription {
    return DeviceEventEmitter.addListener('onProviderChange', callback);
  },

  onActivityChange(callback: (data: any) => void): EmitterSubscription {
    return DeviceEventEmitter.addListener('onActivityChange', callback);
  },

  onMotionChange(callback: (data: any) => void): EmitterSubscription {
    return DeviceEventEmitter.addListener('onMotionChange', callback);
  },

  // Optional: Call this to remove all listeners
  removeAllListeners(): void {
    DeviceEventEmitter.removeAllListeners('onLocationChange');
    DeviceEventEmitter.removeAllListeners('onProviderChange');
    DeviceEventEmitter.removeAllListeners('onActivityChange');
    DeviceEventEmitter.removeAllListeners('onMotionChange');
  },
};

export default BackgroundLocation;
