(ns omkamra.cowbells.tutorial
  (:require [omkamra.cowbells :as cowbells])
  (:require [omkamra.sequencer.targets.fluidsynth]))

;; -[ INTRO ]---------------------------------------------------------

;; Cowbells is a Clojure library for musical experimentation and live
;; coding.

;; To follow this tutorial, you will need:

;; 1. some knowledge of the Clojure programming language
;; 2. the ability to write Clojure programs in your chosen editor
;; 3. the ability to send Clojure forms to an attached REPL for evaluation

;; Hardware and software requirements:

;; 1. Linux (as it has not been tested on any other OS yet)
;; 2. FluidSynth library (https://www.fluidsynth.org/)
;; 3. FluidR3_GM.sf2 soundfont (see below for a download link)
;; 4. Clojure REPL with all project.clj dependencies on its classpath
;; 5. A working sound card

;; If you cannot find FluidR3_GM.sf2 in the package repository of your
;; Linux distribution, a copy can be downloaded from
;; https://musical-artifacts.com/artifacts/738/FluidR3_GM.sf2

;; The code below uses a hard-coded path to FluidR3_GM.sf2. If you
;; have this soundfont at a different location, update the path
;; accordingly.

;; Preparation:

;; 1. Clone this repository: https://github.com/omkamra/cowbells
;; 2. Open tutorial.clj in your Clojure editor
;; 3. Fire up a Clojure REPL and jack in
;; 4. Evaluate the `ns` form at the top

;; If the `ns` form evaluates without errors, you are most likely
;; ready to go.

;; -[ PROJECTS ]------------------------------------------------------

;; First let's create a Cowbells project:

(cowbells/defproject tutorial
  {:target [:fluidsynth "/usr/share/soundfonts/FluidR3_GM.sf2"]})

;; The arguments of the `defproject` macro are the name of the project
;; and a map of configuration settings.

;; The value of the `:target` key is a *target descriptor*: a Clojure
;; data structure (typically a vector) identifying the target device
;; which we want to control.

;; This particular descriptor tells Cowbells that we want to play on a
;; FluidSynth synthesizer and this synth should load the soundfont at
;; the specified path when it starts.

;; If several `defproject` forms refer to the same target descriptor,
;; each of them will get the same target object. (Targets are cached
;; and the target descriptor is used as the cache key.)

;; A project may define any number of targets:

;; (cowbells/defproject multi-target-project
;;   {:targets
;;    {::default
;;     [:fluidsynth "/usr/share/soundfonts/FluidR3_GM.sf2"]
;;     ::fantasia
;;     [:fluidsynth "/usr/share/soundfonts/FantaGM 32 1.0.sf2"]}})

;; These targets can be referenced later by their aliases (here
;; `::default` or `::fantasia`).

;; A target alias must be a keyword qualified with the project
;; namespace.

;; A single target given as the value of the `:target` key will get
;; the alias `::default`.

;; Note that every Cowbells project needs a dedicated namespace. The
;; reason for this is that `defproject` generates a couple of
;; project-specific macros (like `play` or `stop`) and these macros
;; would clash if two projects were defined in the same namespace.

;; -[ PLAY ]----------------------------------------------------------

;; Now that we have a project and a default target which implements
;; the MIDI protocol, we can start playing notes:

(play [:note :c-4])

;; Did you hear something? No?
;;
;; The reason for the silence is that the `defproject` form sets the
;; `silent?` flag of the project to true. While this flag is true,
;; `play` does not do anything. But why would we want this?

;; As it turns out, in a live coding situation it happens pretty often
;; that we open a Cowbells namespace, load the whole file at once and
;; then start evaluating various forms - which cause all sorts of
;; side-effects - one by one.

;; Imagine what would happen if Cowbells allowed all forms to execute
;; their side-effects immediately as we load the file. Cacophony?

;; To prevent this from happening, the `defproject` form sets the
;; `silent?` flag to true and `play` skips its action when `silent?`
;; is true.

;; Obviously, sooner or later we need to turn `silent?` off
;; (presumably at the end of the file, when all forms had been read
;; and evaluated).

;; This is the purpose of the `eof` macro:

;; (eof)

;; Just invoke this macro at the end of your Cowbells namespaces and
;; you will never have a problem with `silent?` again.

;; Now scroll down to the bottom of this namespace, find the `eof`
;; macro, evaluate it, then come back and try to evaluate the above
;; `play` form again.

;; You should hear a C note in octave 4, played on the "Grand Piano"
;; patch (program number 0) of the FluidR3_GM.sf2 soundfont.

;; The thing you passed to `play` is a *pattern expression*: a vector
;; with a keyword at its head which says what kind of expression it is
;; and what should be done with it.

;; Try some more notes:

(play [:note :c-3])
(play [:note :e-3])
(play [:note :g-3])
(play [:note :g#3])

;; If you don't like the keyword notation for pitches, you can also
;; specify them as MIDI note values:

(play [:note 60])

;; -[ GROUPS ]--------------------------------------------------------

;; To play several patterns in a row, use a `:seq` expression:

(play [:seq
       [:note :c-4]
       [:note :e-4]
       [:note :g-4]])

;; If you want to play a set of patterns simultaneously, use `:mix`:

(play [:mix
       [:note :c-4]
       [:note :e-4]
       [:note :g-4]])

;; How about combining the two:

(play [:seq
       [:mix
        [:note :c-4]
        [:note :e-4]
        [:note :g-4]]
       [:mix
        [:note :e-5]
        [:note :g-5]
        [:note :b-5]]])

;; Hmm this didn't really work. It played six notes at once, instead
;; of the two triads one after the other.

;; The reason for this behavior is that `:mix` does not advance the
;; *pattern offset*, even if its sub-expressions do (the pattern
;; offset determines the position/time of the next event which gets
;; compiled into the pattern).

;; To get around this issue, you can insert a `:wait` expression
;; between the two `:mix`es:

(play [:seq
       [:mix
        [:note :c-4]
        [:note :e-4]
        [:note :g-4]]
       [:wait 1]
       [:mix
        [:note :e-5]
        [:note :g-5]
        [:note :b-5]]])

;; As the `[:wait 1]` expression is compiled with the same pattern
;; offset as the first `:mix`, the difference in time between the
;; first and the second `:mix`es will be exactly 1 step (from this you
;; maybe figured out that all what `:wait` does is to increase the
;; pattern offset by the given amount).

;; Alternatively, you may use a `:mix1` expression - this works like
;; `:mix` but allows its first sub-expression (here `[:note :c-4]`) -
;; but not the rest - to advance the pattern offset:

(play [:seq
       [:mix1
        [:note :c-4]
        [:note :e-4]
        [:note :g-4]]
       [:mix
        [:note :e-5]
        [:note :g-5]
        [:note :b-5]]])

;; If you want to get a deeper understanding of what is happening here
;; and how patterns are built in general, try to wrap your head around
;; the documentation of the Omkamra sequencer used by Cowbells at
;; https://github.com/omkamra/sequencer.

;; -[ SCALES, STEPS AND DURATIONS ]-----------------------------------

;; So far you have specified notes using an absolute pitch, e.g. `:e-3` or `60`.

;; An alternative way to do this is through the use of *scale
;; degrees*:

(play [:degree 0])
(play [:degree 1])
(play [:degree 2])

;; By default, Cowbells uses a 12-degree chromatic scale which starts
;; on the C note of octave 5. To change the scale, wrap your patterns
;; in a `:bind` form:

(play
 [:bind {:scale :major}
  [:degree 0]
  [:degree 1]
  [:degree 2]])

;; If you want to use some exotic scale which Cowbells does not yet
;; know about, bind a vector of its intervals to `:scale`:

(play
 (let [my-exotic-scale [0 1 3 5 6 8 10 11]]
   [:bind {:scale my-exotic-scale}
    [:degree 0]
    [:degree 1]
    [:degree 2]]))

;; If you want to change the root of the scale, bind `:root`:

(play
 [:bind {:scale :minor
         :root :f#3}
  [:degree 0]
  [:degree 1]
  [:degree 2]])

;; To change the octave, bind `:oct`:

(play
 [:bind {:root :f#3
         :scale :minor}
  [:seq
   [:degree 0]
   [:degree 1]
   [:degree 2]]
  [:bind {:oct 2}
   [:degree 0]
   [:degree 1]
   [:degree 2]]])

;; Note that `:oct` has a relative value: it expresses how many times
;; 12 semitones (= 1 octave) should be added to the otherwise
;; calculated MIDI note value.

;; `:semi` can be used for semitone shifts:

(play
 [:seq
  [:bind {:semi 0} [:seq [:degree 0] [:degree 2]]]
  [:bind {:semi -3} [:seq [:degree 0] [:degree 2]]]
  [:bind {:semi 5} [:seq [:degree 0] [:degree 2]]]])

;; Let's play all seven notes of the major scale, but now with some
;; help from Clojure:

(play
 [:bind {:scale :major}
  (for [i (range 8)] [:degree i])])

;; (Ok it was eight notes, just that we don't finish on the leading note.)

;; As this example illustrates, if the pattern compiler sees a Clojure
;; sequence that is not a pattern expression, but satifies
;; `sequential?` (like the lazy sequence returned by `for`), it
;; converts that into a `:seq` expression with the same items.

;; The *mode* of scales (Ionian, Dorian, Phrygian, etc.) can be also
;; changed:

(play
 [:bind {:scale :major
         :mode 2}
  (for [i (range 8)] [:degree i])])

;; The value of `:mode` is an integer that defines how many steps
;; to "move right" in the vector of scale intervals to find the root
;; note of the scale. (Originally I called this "shift". It may also
;; be negative.)

;; Let's play some other scales but speed them up a bit and introduce
;; some random variation into the velocity of the individual notes:

(play
 (for [scale [:major :minor :double-harmonic-major :melodic-minor]]
   [:bind {:scale scale
           :step 1/2
           :dur 3/4}
    (for [i (range 8)]
      [:bind {:vel (- 100 (rand-int 20))}
       [:degree i]])]))

;; The value of the `:step` binding determines how much the pattern
;; offset advances after each `:note` or `:degree`. It also specifies
;; the time unit of `:wait`.

;; The value of `:step` is expressed in *beats*. By default the
;; sequencer is ticking at 120 beats per minute (BPM) but this can be
;; changed with a `:bpm` key in the project settings. The default
;; value of `:step` is 1, so initially one step = one beat.

;; The value of the `:dur` binding specifies the *duration* of notes.
;; This is also expressed in beats and is independent from `:step`. In
;; the case of MIDI targets, the duration tells the compiler how far
;; to place the `note off` event in the pattern after the `note on`
;; which triggered the note. If the value of `:dur` is nil (the
;; default), there is no `note off` event written into the pattern at
;; all (this may cause issues with instruments whose notes are not
;; released automatically).

;; `:vel` sets the velocity of the notes (0-127)

;; -[ CHANNELS AND PROGRAMS ]-----------------------------------------

;; Most MIDI devices provide 16 channels, with each channel having its
;; own program. Thus in a traditional MIDI setup you can have 16
;; different instruments playing at once.

;; As FluidSynth is a software synthesizer, it is not bound by this
;; limitation: it can use an arbitrary number of MIDI channels. In the
;; case of Cowbells, the number of channels is set to 256 by default;
;; this lets one use all 128 instruments of a General MIDI soundbank
;; simultaneously.

;; To change the MIDI channel, bind `:channel`:

(play
 [:bind {:scale :minor}
  [:bind {:channel 5}
   [:program 5]
   [:mix
    [:degree 0]
    [:degree 2]
    [:degree 4]]]
  [:bind {:channel 33}
   [:program 33]
   [:seq
    [:degree 0]
    [:degree 2]
    [:degree 4]
    [:bind {:step 1/2}
     [:degree 5]
     [:degree 4]]]]
  [:bind {:channel 52
          :dur 3}
   [:program 52]
   [:degree -7]]])

;; The `:program` expression sends a `program-change` MIDI message on
;; the current channel.

;; With that many MIDI channels available, it is useful to configure
;; channel N with program N in a special pattern that is only used for
;; initialization:

(play
 (for [i (range 128)]
   [:bind {:channel i}
    [:program i]]))

;; Once you evaluate the above `play` form, you don't need to insert
;; `:program` expressions any more: just change the channel to the
;; desired program number and you are set:

(play
 [:bind {:step 1/2
         :dur 4
         :vel 50
         :scale :major}
  (for [channel (range 128)]
    [:bind {:channel channel}
     [:degree (+ -7 (rand-int 15))]])])

;; Although I tried to ensure this last example won't blow up your
;; speakers, some others might.

;; For these cases, it's good to know that you can stop all running
;; targets by executing `(stop)` in the project namespace. One
;; possible way to manage this is to keep a REPL window open by the
;; side that is bound to the project namespace and when things go
;; sour, just evaluate `(stop)` there.

;; -[ FUNCTIONS, VARS, LIVE LOOPS ]-----------------------------------

;; So far we have only played music, but Cowbells patterns can also
;; invoke arbitrary Clojure functions:

(play
 [:seq
  [:degree 0]
  [:call println "Hello!"]
  [:degree 7]])

;; Function objects found inside a pattern expression are
;; automatically wrapped in a `:call` expression:

(play
 [:seq
  [:degree 0]
  #(println "Hello again!")
  [:degree -12]])

;; The ability to embed function calls as sequencer events lets us
;; implement live loops. Consider the following example:

(def my-little-loop
  [:bind {:scale :major}
   [:mix1
    [:bind {:channel 9}
     (repeat 4 [:note 36])]
    [:bind {:channel 38
            :oct -2
            :dur 2}
     [:program 38]
     (repeat 2 [:bind {:step 1/2}
                [:degree 2]
                [:degree -5]
                [:degree -3]])]]
   ;; #(play my-little-loop)
   ])

(play my-little-loop)

;; See that function - the one which would invoke `play` - commented
;; out on the last line? Try to uncomment it, re-evaluate the def and
;; then play my-little-loop again.

;; This is what we call a "live loop" (after Sonic Pi).

;; Why is it live? Try changing something in the definition of
;; my-little-loop and re-evaluate the def form. At the start of the
;; next 4-beat bar the loop should automatically pick up the change.

;; If you grow tired of the endless repetition, either (stop) it or
;; comment out that last function and re-evaluate.

;; To ease experimentation with live loops, Cowbells provides the
;; following macros:

(defp my-four-beat-loop
  [:bind {:channel 9}
   (repeat 4 [:note 36])])

;; The `defp` form is like a `def` followed by `play`.

;; If you want to loop the pattern, change the `defp` to `defp<`:

(defp< my-four-beat-loop
  [:bind {:channel 9}
   (repeat 4 [:note 36])])

;; If you want to stop the looping, change `defp<` back to `defp` and
;; re-evaluate (it will stop when the current loop completes)

;; Tip: If your pattern is very long, changing `defp<` to `defp` may
;; not cut it (it takes too long to stop). In these cases, change the
;; `defp<` to `defp-` first and evaluate - this invokes the `clear!`
;; macro who removes all pending events from the sequencer timeline -
;; and then finally change it back to `defp`.

;; These patterns can be combined into higher level units:

(def drums
  [:bind {:channel 9}
   (repeat 4 [:note 36])])

(def hihats
  [:bind {:channel 9
          :step 1/4}
   (repeat 4 [:seq (repeat 2 [:note 42]) [:note 46]])
   (repeat 4 [:note 42])])

(defp drums+hihats
  [:mix1 #'drums #'hihats])

;; Note that we refer to the `drums` and `hihats` patterns via var
;; references. If we left those out, the live update functionality
;; would stop working. Try to redefine `drums+hihats` as follows and
;; then try to update either `drums` or `hihats` while it is playing:

(defp< drums+hihats
  [:mix1 drums hihats])

;; The reason it doesn't work without vars is that in this case the
;; Clojure compiler inlines the current values of `drums` and `hihats`
;; immediately when it evaluates the `drums+hihats` pattern. If we use
;; vars, then this "inlining" is postponed to the time when the
;; pattern gets built. And as `defp<` repeatedly rebuilds the pattern
;; after every iteration, changes in referenced var bindings are
;; automatically picked up.

;; Note that the pattern form `#'v` is shorthand for the pattern
;; expression `[:var #'v]`. This pattern expression has another nice
;; feature: if the resolved var happens to be a function (but not a
;; pattern transformer function), the compiler applies this function
;; to any extra arguments inside the `:var` form (following the var
;; reference) and compiles the resulting value into a pattern
;; transformer.

;; This feature can be used to implement parameterized pattern
;; generators which can be also updated dynamically while they are
;; used by other patterns:

(defn generate-arp
  [start-degree notes]
  (for [i (range notes)]
    [:degree (+ start-degree (* i 2))]))

(defp< arp-machine
  [:bind {:step 1/3
          :scale :minor
          :channel 114
          :vel 70}
   [:program 114]
   [:var #'generate-arp 0 3]
   [:var #'generate-arp 2 5]
   [:var #'generate-arp -3 3]
   [:var #'generate-arp -7 5]])

;; -[ SNAPPING TO THE GRID]-------------------------------------------

;; Let's return to our little drums+hihats pattern again: start the
;; following loop and then continue reading.

(defp< drums+hihats
  [:mix1 #'drums #'hihats])

;; How about writing a bass part to accompany the drums?

(play
 [:bind {:channel 32
         :oct -3
         :dur 1/2
         :scale :minor}
  [:program 32]
  [:bind {:step 1/2}
   [:degree 4]
   [:degree 5]]
  [:wait 1]
  [:degree 11]
  [:degree 10]
  [:bind {:step 1/2}
   [:degree 7]
   [:wait 1/2]
   [:degree 5]]])

;; As you may recognize, we have to be rather precise with our
;; evaluations to ensure that the bass part starts at the right time,
;; i.e. at the beginning of the 4-beat loop defined by `drums+hihats`.

;; Sometimes this is perfectly ok (we want to play freely, not
;; constrained by any formal structure), but other times we want to
;; ensure that a pattern starts exactly at a given beat.

;; This is made possible by the concept of "snapping".

;; Each pattern has a `snap` value which determines exactly where it
;; gets merged onto the sequencer's timeline when you play it.

;; The snap value is initially zero, this means the merging happens
;; without any adjustments. But if you set the snap value to some
;; non-zero, positive number (expressed in beats), then when that
;; pattern gets merged onto the timeline, its starting offset will be
;; adjusted (increased) if necessary to ensure it is divisible by the
;; value of snap converted into ticks (that is the unit used by the
;; sequencer deep down in the guts of the system).

;; TL;DR: If you want to ensure that two patterns always line up with
;; each other, set their snap value to the same number (preferably the
;; length of the longer one but this is not a requirement).

;; The way to do this is to use a `:snap` expression somewhere inside
;; the pattern:

(play
 [:seq
  [:snap 4]
  [:degree 0]])

;; If you play this pattern a couple of times, you will see that it
;; doesn't always play immediately as you evaluate it. The
;; pattern "waits" until the timeline gets aligned with the snap
;; value.

;; Challenge: add a suitable snap value to the `drums+hihats` and bass
;; patterns above to ensure that the bass always starts at the
;; beginning of the bar.

;; Tip: the project configuration also has a `:snap` key. If you store
;; a snap value there, all patterns you play will automatically
;; inherit that snap value as their default (but you can still
;; override it inside the pattern if necessary.)

;; -[ THE TRANSCRIPTION LANGUAGE ]------------------------------------

;; Having read through all of the stuff I have shown so far, you may
;; get the impression that this is a pretty cool toy, for programmers.
;; But if your goal is to transcribe musical notation - that is, sheet
;; music - into the computer, encoding it into these pattern forms
;; would be (a) a Herculean task and (b) quite painful.

;; As I personally came to this conclusion, I developed another way to
;; describe these pattern forms and pattern expressions. It is not a
;; new language, just a layer on top of what you have already seen, a
;; layer in which the pattern forms and expressions are written in the
;; form of strings.

;; Examples:

(play "0")
;; =
(play [:degree 0])

(play "&(major) 0 2 4")
;; =
(play
 [:bind {:scale :major}
  [:seq
   [:degree 0]
   [:degree 2]
   [:degree 4]]])

(play "{c85 ~16 v60 p85 -7 -14}")
;; =
(play
 [:bind {:channel 85
         :dur 16
         :vel 60}
  [:program 85]
  [:mix1
   [:degree -7]
   [:degree -14]]])

;; The binding modifiers - c85, ~16, etc. - can be placed anywhere
;; inside the {...} brackets. The compiler collects them and puts them
;; into the bind map of the enclosing :bind expression.

(play "{(c1 p1 ./2 m60 m65 m48) (c5 p5 .2/3 m48 m60 m65)}")
;; =
(play
 [:mix1
  [:bind {:channel 1
          :step [:mul 1/2]}
   [:program 1]
   [:note 60]
   [:note 65]
   [:note 48]]
  [:bind {:channel 5
          :step [:mul 2/3]}
   [:program 5]
   [:note 48]
   [:note 60]
   [:note 65]]])

;; `(...)` translates to a `:seq` expression (which may be optimized away)

;; Note that `./2` is translated to "multiply the current value
;; of :step by 1/2", instead of simply setting it to the absolute
;; value 2. The fact that this is relative becomes very useful when we
;; want to speed up or slow down entire parts of a piece by
;; manipulating the `:step` value of the outermost bind.

;; The `:mix` pattern does not have a string representation as I did
;; not find any use for it.

(play "&(minor) 3v+10 ,/2 (c42 p42 7 _1)~4v-20 ,3/2 2 -1.2 0 -11")
;; =
(play
 [:bind {:scale :minor}
  [:bind {:vel [:add 10]}
   [:degree 3]]
  [:wait 1/2]
  [:bind {:dur 4
          :channel 42
          :oct [:sub 1]
          :vel [:sub 20]}
   [:program 42]
   [:degree 7]]
  [:wait 3/2]
  [:degree 2]
  [:bind {:step [:mul 2]}
   [:degree -1]]
  [:degree 0]
  [:degree -11]])

;; Binding modifiers can be attached either to the note / degree / seq
;; / mix / etc. which they modify or written inside the group (in any
;; order).

(play "0_2b3 0_1b 0 0^#4 0^2#2")
;; =
(play
 [:bind {:oct [:sub 2] :semi [:sub 3]} [:degree 0]]
 [:bind {:oct [:sub 1] :semi [:sub 1]} [:degree 0]]
 [:degree 0]
 [:bind {:oct [:add 1] :semi [:add 4]} [:degree 0]]
 [:bind {:oct [:add 2] :semi [:add 2]} [:degree 0]])

(play "3 %4 12 7")
;; =
(play
 [:degree 3]
 [:wait -4]
 [:degree 12]
 [:degree 7])

;; we need % for a negative :wait because ",-4" is interpreted as ", -4"

(play "&(major) (0 2 4) (0 2 4)>5")
;; =
(play
 [:bind {:scale :major}
  [:seq
   [:degree 0]
   [:degree 2]
   [:degree 4]]
  [:bind {:mode 5}
   [:seq
    [:degree 0]
    [:degree 2]
    [:degree 4]]]])

(play "@g#3 0 1")
;; =
(play "(0 1)@g#3")
;; =
(play
 [:bind {:root :g#3}
  [:degree 0]
  [:degree 1]])

(play "&(major) (0 2) (0 2)@4")
;; =
(play
 [:bind {:scale :major}
  [:seq
   [:degree 0]
   [:degree 2]]
  [:bind {:root [:degree->key 4]}
   [:degree 0]
   [:degree 2]]])

;; Here we bind the root note to the 4th degree of the current
;; scale (major) and then play degrees 0 and 2 of the major scale
;; starting there.

;; -[ CLOSING WORDS ]-------------------------------------------------

;; Hopefully this will make someone happy.

;; If you find any issue (I'm sure there are many) or have anything to
;; say about this, feel free to open an issue on GitHub.

;; Happy looping,
;;  cellux

(eof)
