# Kether

A script-like quest plugin for Minecraft servers.

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org) [![Build status](https://img.shields.io/appveyor/build/IzzelAliz/kether)](https://ci.appveyor.com/project/IzzelAliz/kether)

## Installing

Download the plugin where:

* Stable releases in the [Release](https://github.com/IzzelAliz/Kether/releases) page
* Development builds in [appveyor](https://ci.appveyor.com/project/IzzelAliz/kether/build/artifacts)

and simply drop them into plugins folder.

## Contributing

Clone this repository to your machine:

```
git clone https://github.com/IzzelAliz/Kether
```

We recommend to use IntelliJ IDEA as IDE.

## Building

* [Gradle](https://gradle.org/) - Dependency Management

The GradleWrapper in included in this project.

On Windows:

```
gradlew.bat clean build shadowJar
```

On MacOS/Linux:

```
./gradlew clean build shadowJar
```

Build artifacts should be found in `./build/libs` folder.

## Versioning

We use [SemVer](http://semver.org/) for versioning.

## License

This project is licensed under the MIT License, see the [LICENSE](LICENSE) file for details
