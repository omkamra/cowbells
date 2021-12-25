# omkamra.cowbells

[![Clojars Project](https://img.shields.io/clojars/v/com.github.omkamra/cowbells.svg)](https://clojars.org/com.github.omkamra/cowbells)

A Clojure library for musical experimentation and live coding.

Currently Linux only (but you may be able to make it work on your platform).

Feature highlights:

- drives an in-process FluidSynth instance through the C API bound into the JVM via [JNR-FFI](https://github.com/omkamra/jnr)
- notes can be represented by keywords (`:c-3`), MIDI note values or scale degrees
- supports any kind of scale (just give it the list of intervals)
- musical phrases can be encoded with either Clojure data structures or an equivalent text-based syntax
- musical phrases can be bound to variables for reuse in other phrases
- supports scoped change of scale, mode, root note, octave, semitone offset, MIDI channel, velocity, note step and duration
- supports live looping (in the sense of Extempore or Sonic Pi)
- the sequencer is pretty generic, it could be used to control anything, not just musical devices (e.g. it would be possible to play music and control an OpenGL visualizer via OSC simultaneously from the same patterns)

If you are interested, read the [tutorial](https://github.com/omkamra/cowbells/blob/master/src/omkamra/cowbells/tutorial.clj).

## License

Copyright © 2021 Balázs Ruzsa

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
