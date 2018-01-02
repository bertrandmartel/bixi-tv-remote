# Bixi TV remote

[![CircleCI](https://img.shields.io/circleci/project/bertrandmartel/bixi-tv-remote.svg?maxAge=2592000?style=plastic)](https://circleci.com/gh/bertrandmartel/bixi-tv-remote)
[![License](http://img.shields.io/:license-mit-blue.svg)](LICENSE.md)

An Android TV app using [Bixi](https://bixi.io/) device as a TV remote using [bixi-android library](https://github.com/bertrandmartel/bixi-android)

**Android TV device must be rooted with Super Su installed to be able to inject keycodes**

## Actions

| Bixi event            | keycode/actions  |
|-----------------------|----------|
| CENTER_TO_TOP         | KEYCODE_DPAD_UP |
| CENTER_TO_BOTTOM      | KEYCODE_DPAD_DOWN |
| CENTER_TO_LEFT        | KEYCODE_DPAD_LEFT |
| CENTER_TO_RIGHT       | KEYCODE_DPAD_RIGHT |
| LINEAR_CHANGE         | set volume |
| DOUBLE_TAP            | KEYCODE_ENTER |

The caveats :

* Bixi device has not enough events to map the back button & home button
* Bixi events are detected too slowly (and subject to gesture mistakes)
* there are no state (eg to be able to map keys down/up) for the `CENTER_`

## Requirements

Android TV

## License

```
The MIT License (MIT) Copyright (c) 2017-2018 Bertrand Martel
```