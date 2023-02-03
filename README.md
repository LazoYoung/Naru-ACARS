# SimOverlayNG
> This project is under development.

SimOverlayNG is a software creating livestream overlays for flight simmers.

# Prerequisite
SimOverlayNG is written in Java 17. It uses FSUIPC to communicate with your simulator.

- [Java 17+](https://www.oracle.com/java/technologies/downloads/)
- [FSUIPC](http://www.fsuipc.com/)

# Planned features
## Short term
- Reads simulator data through FSUIPC
  - Track distance (NM, km)
  - ETA
  - Departure/Arrival airport (ICAO, IATA, Name, City)
  -	Aircraft type (ICAO)
  - Aircraft name
  -	Altitude (ft, m)
  -	Heading (MAG, TRU)
  -	Ground/Air speed (knot, km/h, mph)
  -	Vertical speed (fpm, m/s)
  -	Sim/Real time (UTC)
  -	Latitude/Longitude
  - ... and more
- Stock overlay templates
  - HUD
  - Platform display
  - Boarding pass
- Animated, changing text
- Customizable text including data placeholders

## Long term
- Overlay template editor
- Landing rate widget

# Legal notice
SimOverlayNG copyright by LazoYoung. Feel free to contact me if you'd like to modify, redistribute, or contribute to this project.

This software depends on [FSUIPC-Java by Mouseviator](https://github.com/Mouseviator/FSUIPC-Java) which is licensed under [LGPL v3.0](https://github.com/Mouseviator/FSUIPC-Java/blob/master/LICENSE.txt).
