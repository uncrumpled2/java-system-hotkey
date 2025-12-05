# Clojure Usage Example

Add the dependency to your `deps.edn`:

```clojure
{:deps {com.systemhotkey/system-hotkey {:local/root "../java-system-hotkey"}}}
```

Or if published to a repository:

```clojure
{:deps {com.systemhotkey/system-hotkey {:mvn/version "0.1.0"}}}
```

## Basic Usage

```clojure
(ns myapp.hotkeys
  (:import [com.systemhotkey SystemHotkey Hotkey Modifier Key]))

(defn run-hotkey-demo []
  (let [hotkey (SystemHotkey/create)
        ctrl-shift (bit-or Modifier/CONTROL Modifier/SHIFT)
        hotkey-a (Hotkey/of ctrl-shift Key/A)
        hotkey-q (Hotkey/of ctrl-shift Key/Q)]
    (try
      ;; Register hotkeys
      (.register hotkey hotkey-a)
      (.register hotkey hotkey-q)
      (println "Listening for Ctrl+Shift+A and Ctrl+Shift+Q...")

      ;; Event loop
      (loop []
        (let [triggered (.poll hotkey)]
          (doseq [h triggered]
            (println "Triggered:" (.toString h)))
          (when-not (some #(.equals % hotkey-q) triggered)
            (Thread/sleep 10)
            (recur))))

      (finally
        (.close hotkey)))))
```

## With core.async

```clojure
(ns myapp.hotkeys-async
  (:require [clojure.core.async :as async :refer [<! >! go-loop chan]])
  (:import [com.systemhotkey SystemHotkey Hotkey Modifier Key]))

(defn start-hotkey-listener
  "Returns a channel that emits triggered hotkeys."
  [hotkeys-to-register]
  (let [out-chan (chan 32)
        hotkey (SystemHotkey/create)]

    ;; Register all hotkeys
    (doseq [hk hotkeys-to-register]
      (.register hotkey hk))

    ;; Start polling in background
    (go-loop []
      (let [triggered (.poll hotkey)]
        (doseq [h triggered]
          (>! out-chan h))
        (async/<! (async/timeout 10))
        (recur)))

    {:channel out-chan
     :context hotkey}))

(defn stop-hotkey-listener [{:keys [channel context]}]
  (async/close! channel)
  (.close context))

;; Usage:
(comment
  (def ctrl-a (Hotkey/of Modifier/CONTROL Key/A))
  (def listener (start-hotkey-listener [ctrl-a]))

  ;; In another go block or thread:
  (go-loop []
    (when-let [h (<! (:channel listener))]
      (println "Got hotkey:" h)
      (recur)))

  ;; Cleanup:
  (stop-hotkey-listener listener))
```

## Functional wrapper

```clojure
(ns myapp.hotkeys-wrapper
  (:import [com.systemhotkey SystemHotkey Hotkey Modifier Key]))

(defn hotkey
  "Create a hotkey. Modifiers can be keywords or ints."
  [mods key]
  (let [mod-map {:ctrl Modifier/CONTROL
                 :control Modifier/CONTROL
                 :shift Modifier/SHIFT
                 :alt Modifier/ALT
                 :super Modifier/SUPER
                 :cmd Modifier/SUPER
                 :win Modifier/SUPER}
        key-map {:a Key/A :b Key/B :c Key/C :d Key/D :e Key/E
                 :f Key/F :g Key/G :h Key/H :i Key/I :j Key/J
                 :k Key/K :l Key/L :m Key/M :n Key/N :o Key/O
                 :p Key/P :q Key/Q :r Key/R :s Key/S :t Key/T
                 :u Key/U :v Key/V :w Key/W :x Key/X :y Key/Y
                 :z Key/Z
                 :space Key/SPACE :enter Key/ENTER
                 :escape Key/ESCAPE :esc Key/ESCAPE
                 :tab Key/TAB
                 :f1 Key/F1 :f2 Key/F2 :f3 Key/F3 :f4 Key/F4
                 :f5 Key/F5 :f6 Key/F6 :f7 Key/F7 :f8 Key/F8
                 :f9 Key/F9 :f10 Key/F10 :f11 Key/F11 :f12 Key/F12}
        resolve-mod (fn [m] (if (keyword? m) (get mod-map m m) m))
        resolve-key (fn [k] (if (keyword? k) (get key-map k k) k))
        mods-int (if (coll? mods)
                   (reduce bit-or 0 (map resolve-mod mods))
                   (resolve-mod mods))]
    (Hotkey/of mods-int (resolve-key key))))

;; Usage:
(comment
  (hotkey [:ctrl :shift] :a)  ; => Hotkey for Ctrl+Shift+A
  (hotkey :ctrl :c)           ; => Hotkey for Ctrl+C
  (hotkey [:cmd :shift] :q))  ; => Hotkey for Cmd+Shift+Q (macOS)
```
