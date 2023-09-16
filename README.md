
<img width="403" alt="banner" src="logo.png">

Logo designed by [@wnynya](https://github.com/wnynya)

## About
NaruACARS is a versatile app that connects to your flight simulator.

## Features
- ACARS reporting system for virtual airliners
- Live overlay generator (HTML based)

<img width="500" alt="demo" src="demo.gif">

## Links
- [Download](https://github.com/LazoYoung/NaruACARS/releases)
- [Documentation](https://github.com/LazoYoung/NaruACARS/wiki)
- [Roadmap](https://github.com/LazoYoung/NaruACARS/wiki/Roadmap)

[Java 17+](https://www.oracle.com/java/technologies/downloads/) and a simulator bridge is required to use this software.

## Legal notice
NaruACARS &#169; 2023 LazoYoung, All rights reserved.

This application is proudly powered by these amazing works...!

- [FSUIPC-Java](https://github.com/Mouseviator/FSUIPC-Java) by Mouseviator, licensed under [LGPL v3.0](https://github.com/Mouseviator/FSUIPC-Java/blob/master/LICENSE.txt)
- [XPlaneConnect](https://github.com/nasa/XPlaneConnect) by NASA
- Airline database from [openflights.org](https://github.com/jpatokal/openflights/blob/master/data/LICENSE)
- Airport database by [David Megginson](https://github.com/davidmegginson/ourairports-data)

## Packaging
### For Windows
WiX Toolset is required for this to work.
```
jpackage --input target --name Naru-ACARS --app-version 1.1 --icon Naru-ACARS.ico --main-jar Naru-ACARS.jar --type msi --win-dir-chooser --win-shortcut --win-per-user-install
```