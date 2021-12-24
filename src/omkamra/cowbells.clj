(ns omkamra.cowbells
  (:require [omkamra.sequencer :as sequencer]
            [omkamra.sequencer.protocols.Target :as Target]))

(def default-sequencer
  (sequencer/create))

(defmacro defproject
  [project-name project-options]
  (assert (and (map? project-options)
               (or (map? (:targets project-options))
                   (:target project-options))))
  (let [default-alias (keyword (str (ns-name *ns*)) "default")
        targets (or (:targets project-options)
                    {default-alias (:target project-options)})]
    (assert (> (count targets) 0)
            "no targets")
    (assert (every? qualified-keyword? (keys targets))
            "target aliases should be qualified keywords")
    (doseq [alias (keys targets)]
      (when-not (= (str (ns-name *ns*)) (namespace alias))
        (throw (ex-info "target alias must be qualified with the project namespace"
                        {:target-alias alias}))))
    (letfn [(define-project-config []
              `(def ~project-name
                 {:sequencer ~(:sequencer project-options `default-sequencer)
                  :targets ~(reduce-kv (fn [targets k v]
                                         (assoc targets k `(sequencer/make-target ~v)))
                                       {}
                                       (or (:targets project-options)
                                           {default-alias (:target project-options)}))
                  :bindings ~(:bindings project-options {})
                  :bpm ~(:bpm project-options 120)
                  :snap ~(:snap project-options 0)
                  :silent? (atom true)}))
            (register-targets [start?]
              `(doseq [[~'alias ~'target] (:targets ~project-name)]
                 (sequencer/register-target ~'target ~'alias)
                 ~@(when start?
                     (list `(when (:playing (sequencer/status (:sequencer ~project-name)))
                              (Target/start ~'target))))))
            (define-helpers []
              `(do
                 (defn ~'clear!
                   []
                   (sequencer/clear! (:sequencer ~project-name)))
                 (defn ~'start
                   []
                   (Target/start (:sequencer ~project-name)))
                 (defn ~'stop
                   []
                   (sequencer/clear! (:sequencer ~project-name))
                   (Target/stop (:sequencer ~project-name)))
                 (defn ~'restart
                   []
                   (Target/restart (:sequencer ~project-name)))
                 (defn ~'play
                   [& ~'forms]
                   (when-not @(:silent? ~project-name)
                     (sequencer/play
                      (:sequencer ~project-name)
                      [:bind (merge (:bindings ~project-name)
                                    {:target (-> ~project-name :targets ~default-alias)})
                       [:bpm (:bpm ~project-name)]
                       [:snap (:snap ~project-name)]
                       `[:seq ~@~'forms]])))
                 (defmacro ~'defp
                   ~'[pattern-name & body]
                   (let [~'v (resolve ~'pattern-name)
                         ~'looping? (::looping? (meta ~'v))
                         ~'result (if ~'v :updated :defined)]
                     `(do
                        (def ~~'pattern-name [:seq ~@~'body])
                        ~@(when-not ~'looping?
                            (list `(~'~'play ~~'pattern-name)))
                        ~~'result)))
                 (defmacro ~'defp<
                   ~'[pattern-name & body]
                   (let [~'v (resolve ~'pattern-name)
                         ~'looping? (::looping? (meta ~'v))
                         ~'result (if ~'v :updated :looping)]
                     `(do
                        ;; pre-compile the pattern to avoid unnecessary
                        ;; recompilation at every loop iteration
                        (def ~~'pattern-name (sequencer/compile-pattern-expr
                                              [:bind
                                               (merge (:bindings ~'~project-name)
                                                      {:target (-> ~'~project-name
                                                                   :targets
                                                                   ~'~default-alias)})
                                               [:seq ~@~'body [:play (var ~~'pattern-name)]]]))
                        (when-not @(:silent? ~'~project-name)
                          (alter-meta! (var ~~'pattern-name) assoc ::looping? true))
                        ~@(when-not ~'looping?
                            (list `(~'~'play ~~'pattern-name)))
                        ~~'result)))
                 (defmacro ~'defp-
                   ~'[& args]
                   `(~'~'clear!))
                 (defn ~'eof
                   []
                   (reset! (:silent? ~project-name) false))))]
      `(do
         ~@(if (resolve project-name)
             (list
              (define-project-config)
              (register-targets true)
              (define-helpers))
             (list
              (define-project-config)
              (register-targets false)
              (define-helpers)))
         #'~project-name))))
