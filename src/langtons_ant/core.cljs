(ns langtons-ant.core
  (:require [cljs.core.async])
  (:require-macros [cljs.core.async.macros :as am :refer [go]]))

(def debug false)

(defn log [& args]
  (when debug
    (doseq [arg args]
      (.log js/console (str arg)))))

(def counter (atom 0))
(def colors (atom []))
(def pixel-ratio 2)

(comment
  {:current-state {:current-color {:rotate "rotation (L R N U)"
                                   :color "set color to"
                                   :state "next state"}}}
  L - "90 degree left"
  R - "90 degree right"
  N - "none"
  U - "180 degree")

(def decision-tables [
                      {0 {0 {:rotate :L :new-color 1 :new-state 0}
                          1 {:rotate :R :new-color 0 :new-state 0}}
                      1  {0 {:rotate :N :new-color 0 :new-state 0}
                          1 {:rotate :N :new-color 0 :new-state 0}}}

                      {0 {0 {:rotate :R :new-color 1 :new-state 0}
                          1 {:rotate :R :new-color 1 :new-state 1}}
                      1  {0 {:rotate :N :new-color 0 :new-state 0}
                          1 {:rotate :N :new-color 0 :new-state 1}}}

                      {0 {0 {:rotate :R :new-color 1 :new-state 1}
                          1 {:rotate :L :new-color 0 :new-state 1}}
                       1 {0 {:rotate :N :new-color 1 :new-state 0}
                          1 {:rotate :N :new-color 0 :new-state 0}}}

                      {0 {0 {:rotate :L :new-color 1 :new-state 1}
                          1 {:rotate :L :new-color 0 :new-state 1}}
                       1 {0 {:rotate :R :new-color 1 :new-state 1}
                          1 {:rotate :L :new-color 0 :new-state 0}}}

                      {0 {0 {:rotate :R :new-color 1 :new-state 1}
                          1 {:rotate :R :new-color 0 :new-state 1}}
                       1 {0 {:rotate :N :new-color 1 :new-state 0}
                          1 {:rotate :N :new-color 1 :new-state 1}}}
                      ])

(def decisions-table (rand-nth decision-tables))

(defn draw-color [x y color context]
  (let [style (if (= 1 color)
                "rgb(0, 0, 0)"
                "rgb(255, 255, 255)")]
    (aset context "strokeStyle" style)
    (aset context "fillStyle" style))
  (.fillRect context (* pixel-ratio x) (* pixel-ratio y) pixel-ratio pixel-ratio))

(defn set-in [coll coords value]
  (update-in coll coords (fn [_] value)))

(defn init-colors [width height]
  (reset! colors (vec (repeat (inc height)
                              (vec (repeat (inc width) 0))))))

(defn set-color [x y color context]
  (draw-color x y color context)
  (swap! colors #(set-in % [x y] color)))

(defn get-color [x y]
  (log (str "pos = " [x y]))
  (get-in @colors [x y]))

(defn rotate-turmite [rotate orientation]
  (log rotate)
  (let [rotation (condp = rotate
                   :L -90
                   :R 90
                   :N 0
                   :U 180)
        v (+ rotation orientation)]
    (cond
      (<= 360 v) (- v 360)
      (< v 0) (+ v 360)
      :else v)))

(defn update-turmite [[x y] {:keys [state orientation]} context]
  (let [color (get-color x y)
        {:keys [new-color
                new-state
                rotate]} (get-in decisions-table [state color])]
    (log (str "state color = " [state color]))
    (log (get-in decisions-table [state color]))
    (set-color x y new-color context)
    {:state new-state :orientation (rotate-turmite rotate orientation)}))

(defn move-turmite [{:keys [orientation]} pos]
  (let [delta (condp = orientation
                0   [0 -1]
                90  [1 0]
                180 [0 1]
                270 [-1 0])
        new-pos (map + pos delta)]
    new-pos))

(defn run-one-move [pos turmite check-pos-fn context]
  (swap! counter inc)
  (when (zero? (rem @counter 100)) (aset js/document "title" (str "move: " @counter)))
  (let [new-turmite (update-turmite pos turmite context)
        new-pos (move-turmite new-turmite pos)]
    (if (check-pos-fn new-pos)
      (js/setTimeout #(run-one-move new-pos
                                    new-turmite
                                    check-pos-fn
                                    context) 1)
      ;(recur new-pos new-turmite check-pos-fn context)
      (log :done (str "old: " pos " new: " new-pos)))))

(defn run-turmite [init-color init-state init-x init-y check-pos-fn context]
  (set-color init-x init-y init-color context)
  (run-one-move [init-x init-y]
                {:state init-state :orientation 0}
                check-pos-fn
                context))

(defn def-poschecking-fn [width height]
  (fn [[x y]]
    (and (<= x width)
         (<= y height)
         (>= x 0)
         (>= y 0))))

(let [canvas (.getElementById js/document "turmite")
      width (-> (aget canvas "width") (/ pixel-ratio) int)
      height (-> (aget canvas "height") (/ pixel-ratio) int)
      context (.getContext canvas "2d")
      check-pos-fn (def-poschecking-fn width height)]
  (init-colors width height)
  (run-turmite (rand-int 2)
               (rand-int 2)
               (int (/ width 2))
               (int (/ height 2))
               check-pos-fn
               context))
