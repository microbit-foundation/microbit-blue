# micro:bit Blue

## Version: 1.5.4


 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.


__Description:__ 

This application contains a series of demonstrations which use the BBC micro:bit Bluetooth profile in various ways. It's purpose is both to act as a demo and to provide a source of example code which shows how to use the Bluetooth profile from Android. 

__Instructions:__<br>

Install the latest apk file from the releases folder. Note that the application requires at least version 2.0.0 of the micro:bit run time and the smartphone or tablet should be paired with the micro:bit before the application can be used.  

__Requirements:__<br>

The application should work on Android version 4.4 or later. Version 5 or later is recommended however. Limited testing has been carried out across Android versions however so please report any issues found.

__Known Issues:__

1. Needs a demo of analogue output
2. Needs a demo of digital input
3. Needs a demo of analogue input
4. Sometimes the digital output demo is unable to determine the initial state of pin 0
5. Animal Vegetable Mineral demo: If you disconnect from the micro:bit then reconnect you will not receive indication messages from the micro:bit any more. Reset the micro:bit and reconnect to solve this. Awaiting new micro:bit API to avoid the underlying issue.  

__Version History:__

__1.5.4__
- It's now possible to switch off data smoothing in the Accelerometer screen to see the effect it has

__1.5.3__
- Bug fix: out of range x/y coordinates sometimes broke touch processing in game controller.

__1.5.2__
- Bug fix: bug processing touch events in game controller caused rare crashes.

__1.5.1__
- Bug fix: application crashed when launched if Bluetooth was not already switched on

__1.5.0__
- Added further information to the main help screen

__1.4.9__
- Changed application name to micro:bit Blue

__1.4.8__
- Added the Trivia Scoreboard demo

__1.4.7__
- Swapped UART TX and RX UUIDs to be in line with Nordic Semiconductor official documentation. Note that this was correct with respect to earlier versions of the micro:bit runtime code. An underlying issue regarding the UART service, its characteristics and their properties has since been fixed and so the Animal Vegetable Mineral demo will only work with a hex file build from this later code base. The fix in question went into github master on 25th May 2016.

__1.4.6__
- Added Refresh Services menu item to the demo Menu Activity. GATT services (and characteristics and descriptors) discovered when connecting to a micro:bit are cached by Android. There's currently no way to tell Android that the GATT services 9etc) have changed because your micro:bit code has changed and so this menu item lets you manually initiate flushing of the cache and a full service (re)discovery process. If you get an error message saying a demo cannot be used because your micro:bit doesn't have a required Bluetooth service and you are 100% convinced it does have it, try using the Refresh Services menu option.

__1.4.5__
- Added UART service Animal Vegetable Mineral demo
- Tightened up closing of BluetoothGatt object on disconnect in the hope that this will improve realisbility on older Android versions
- Guarded execution of some operations in BleAdapterService according to the connection state
- moved wrapping of UI text messages originating from the BleAdapterService to the Activity that receives them

__1.4.4__
- Added simple interpretation of Magnetometer Bearing data, display the nearest point of the compass

__1.4.3__
- Included device Bluetooth address in scan results list to make it possible to distinguish between multiple paired micro:bits

__1.4.2__

- Removed redundant DualDPadControllerActivity.java
- Fixed game controller event code initialisation bug
- Fixed bug which resulted in attempts to pair with an unpaired micro:bit when the main settings screen had deselected filtering of unpaired devices i.e. we want to be able to work with unpaired micro:bits probably because we're developing and testing.
- Fixed bug in multi-touch handling in the dual d-pad game controller

__1.4.1__

- Display current heart rate instead of histogram every 10 seconds

__1.4.0__

- Added Heart Rate Histogram demo

__1.3.1__

- Improved D-Pad Controller screen with proper graphic
- Introduced vibration for feedback when using the D-Pad Controller
- Introduced multi-touch support for the D-Pad Controller
- Event Code and Event Values used by the D-Pad Controller are now configurable in a Settings screen.
- Tidied up layout of some screens to work better across Android device types

__1.3.0__

- Added Dual D-Pad Controller demo
- Added screen orientation properties for all activities
- Changed keep alive function to read the firmware revision string characteristic since the hardware revision string characteristic is being removed from the profile
- Removed hardware revision string from the device information screen

__1.2.0__

- Added Bluetooth Services menu item to the menu page. Produces a report showing which Bluetooth services are present on or absent from the connected micro:bit
__1.2.0__

- Modified to work with the "out of box" general release of the micro:bit runtime which uses Bluetooth security including pairing and white listing.

__1.1.0__

- Uses Android 5 scanning APIs if on 5.0 or later else uses old scanning APIs

__1.0.0__ 

- Initial version which used the Android 4.x scanning APIs

## Contributing

Pull Requests are not being accepted at this time. If you find a bug or have an idea for a new feature, please submit details in the issue tracker. In the case of new features, it may be possible to collaborate on the development. This will be assessed on a case by case basis.

Thank you