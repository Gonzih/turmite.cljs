(ns langtons-ant.core)

(def debug false)

(defn log [& args]
  (when debug
    (.log js/console (apply str args))))

(def counter (atom 0))
(def colors (atom []))
(def pixel-ratio 4)
(def timeout 40)

(comment
  {:current-state {:current-color {:rotate "rotation (L R N U)"
                                   :color "set color to"
                                   :state "next state"}}}
  L - "90 degree left"
  R - "90 degree right"
  N - "none"
  U - "180 degree")

  ;var gGoodActions = {

                      ;newcolor|rotate|newstate
                      ;00   01  10  11

  ;}

(def available-patterns [
                         ;+"FIBONACCI":   "1L1,1L1,1R1,0N0",
                         {0 {0 {:rotate :L :new-color 1 :new-state 1}
                             1 {:rotate :L :new-color 1 :new-state 1}}
                          1 {0 {:rotate :R :new-color 1 :new-state 1}
                             1 {:rotate :N :new-color 0 :new-state 0}}
                          :name "Fibonacci"}
                         ;+"LANGTON":     "1R0,0L0,1R0,0L0",
                         {0 {0 {:rotate :R :new-color 1 :new-state 0}
                             1 {:rotate :L :new-color 0 :new-state 0}}
                          1 {0 {:rotate :R :new-color 1 :new-state 0}
                             1 {:rotate :L :new-color 0 :new-state 0}}
                          :name "Langton"}
                         ;Chaotic 1
                         ;"CHAOTIC2":    "1L0,1N1,1N0,0N1",
                         ;"CHAOTIC3":    "1R1,0L1,1N0,0N0",
                         ;"CHAOTIC4":    "1L1,0R0,0N0,0R1",
                         ;"CHAOTIC5":    "0R1,0R1,1L1,0N0",
                         {0 {0 {:rotate :R :new-color 1 :new-state 0}
                             1 {:rotate :R :new-color 1 :new-state 1}}
                          1 {0 {:rotate :N :new-color 0 :new-state 0}
                             1 {:rotate :N :new-color 0 :new-state 1}}
                          :name "Chaotic 1"}
                         ;Chaotic 2
                         {0 {0 {:rotate :R :new-color 1 :new-state 1}
                             1 {:rotate :L :new-color 0 :new-state 1}}
                          1 {0 {:rotate :N :new-color 1 :new-state 0}
                             1 {:rotate :N :new-color 0 :new-state 0}}
                          :name "Chaotic 2"}
                         ;Chaotic 3
                         {0 {0 {:rotate :L :new-color 1 :new-state 1}
                             1 {:rotate :L :new-color 0 :new-state 1}}
                          1 {0 {:rotate :R :new-color 1 :new-state 1}
                             1 {:rotate :L :new-color 0 :new-state 0}}
                          :name "Chaotic 3"}
                         ;Chaotic 4
                         {0 {0 {:rotate :R :new-color 1 :new-state 1}
                             1 {:rotate :R :new-color 0 :new-state 1}}
                          1 {0 {:rotate :N :new-color 1 :new-state 0}
                             1 {:rotate :N :new-color 1 :new-state 1}}
                          :name "Chaotic 4"}
                         ;"CORAL":       "1R1,1L1,1R1,0R0",
                         {0 {0 {:new-color 1 :rotate :R :new-state 1}
                             1 {:new-color 1 :rotate :L :new-state 1}}
                          1 {0 {:new-color 1 :rotate :R :new-state 1}
                             1 {:new-color 0 :rotate :L :new-state 0}}
                          :name "Coral"}
                         ;"SQUARE1":     "1L0,1R1,0R0,0L1",
                         {0 {0 {:new-color 1 :rotate :L :new-state 0}
                             1 {:new-color 1 :rotate :R :new-state 1}}
                          1 {0 {:new-color 0 :rotate :R :new-state 0}
                             1 {:new-color 0 :rotate :L :new-state 1}}
                          :name "Square 1"}
                         ;"SQUARE2":     "0R1,0L0,1N0,1U1"
                         {0 {0 {:new-color 0 :rotate :R :new-state 1}
                             1 {:new-color 0 :rotate :L :new-state 0}}
                          1 {0 {:new-color 1 :rotate :N :new-state 0}
                             1 {:new-color 1 :rotate :U :new-state 1}}
                          :name "Square 2"}
                         ;"COUNTER1":    "0N1,0U1,1R1,0N1",
                         {0 {0 {:new-color 0 :rotate :N :new-state 1}
                             1 {:new-color 0 :rotate :U :new-state 1}}
                          1 {0 {:new-color 1 :rotate :R :new-state 1}
                             1 {:new-color 0 :rotate :N :new-state 1}}
                          :name "Counter 1"}
                         ;"COUNTER2":    "1R1,0N1,0N0,1L1",
                         {0 {0 {:new-color 1 :rotate :R :new-state 1}
                             1 {:new-color 0 :rotate :N :new-state 1}}
                          1 {0 {:new-color 0 :rotate :N :new-state 0}
                             1 {:new-color 1 :rotate :L :new-state 1}}
                          :name "Counter 2"}
                         ;"SPIRAL1":     "1N1,1L0,1R1,0N0",
                         {0 {0 {:new-color 1 :rotate :N :new-state 1}
                             1 {:new-color 1 :rotate :L :new-state 0}}
                          1 {0 {:new-color 1 :rotate :R :new-state 1}
                             1 {:new-color 0 :rotate :N :new-state 0}}
                          :name "Spiral 1"}
                         ;"SPIRAL2":     "1L0,0R1,1R0,0L1",
                         {0 {0 {:new-color 1 :rotate :L :new-state 0}
                             1 {:new-color 0 :rotate :R :new-state 1}}
                          1 {0 {:new-color 1 :rotate :R :new-state 0}
                             1 {:new-color 0 :rotate :L :new-state 1}}
                          :name "Spiral 2"}
                         ;"SPIRAL3":     "1U0,0N1,0L0,0R1",
                         {0 {0 {:new-color 1 :rotate :U :new-state 0}
                             1 {:new-color 0 :rotate :N :new-state 1}}
                          1 {0 {:new-color 0 :rotate :L :new-state 0}
                             1 {:new-color 0 :rotate :R :new-state 1}}
                          :name "Spiral 3"}
                         ;"LADDER":      "0N1,1U1,1L0,1N1",
                         {0 {0 {:new-color 0 :rotate :N :new-state 1}
                             1 {:new-color 1 :rotate :U :new-state 1}}
                          1 {0 {:new-color 1 :rotate :L :new-state 0}
                             1 {:new-color 1 :rotate :N :new-state 1}}
                          :name "Ladder"}
                         ;"DIXIE":       "0R1,0L0,1U1,0R0",
                         {0 {0 {:new-color 0 :rotate :R :new-state 1}
                             1 {:new-color 0 :rotate :L :new-state 0}}
                          1 {0 {:new-color 1 :rotate :U :new-state 1}
                             1 {:new-color 0 :rotate :R :new-state 0}}
                          :name "Dixe"}
;"DIAMOND":     "1L0,0R1,0R0,1R0",
{0 {0 {:new-color 1 :rotate :L :new-state 0}
    1 {:new-color 0 :rotate :R :new-state 1}}
 1 {0 {:new-color 0 :rotate :R :new-state 0}
    1 {:new-color 1 :rotate :R :new-state 0}}
 :name "Diamond"}
])

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
  (swap! colors #(set-in % [x y] color)))

(defn get-color [x y]
  (get-in @colors [x y]))

(defn display-turmite [[x y] context]
  (aset context "fillStyle" "rgb(200, 0, 0)")
  (.fillRect context (* pixel-ratio x) (* pixel-ratio y) pixel-ratio pixel-ratio))

(defn display-move [[x y] context]
  (draw-color x y (get-color x y) context))

(defn rotate-turmite [rotate orientation]
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

(defn update-turmite [[x y] {:keys [state orientation]} context decisions-table]
  (let [color (or (get-color x y) 0)
        {:keys [new-color
                new-state
                rotate]} (get-in decisions-table [state color])]
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

(defn run-one-move [pos turmite check-pos-fn context decisions-table]
  (try
    (display-move pos context)
    (swap! counter inc)
    ;(when (or (= 1 @counter) (zero? (rem @counter 100))) (log (str "move: " @counter)))
    (let [new-turmite (update-turmite pos turmite context decisions-table)
          new-pos (move-turmite new-turmite pos)]
      (if (check-pos-fn new-pos)
        (do
          (display-turmite new-pos context)
          (js/setTimeout #(run-one-move new-pos
                                        new-turmite
                                        check-pos-fn
                                        context
                                        decisions-table) timeout))
        ;(recur new-pos new-turmite check-pos-fn context)
        (log (str :done " old: " pos " new: " new-pos))))
    (catch js/RangeError e (log "Catched " e))))

(defn run-turmite [init-color init-state init-x init-y check-pos-fn context decisions-table]
  (set-color init-x init-y init-color context)
  (run-one-move [init-x init-y]
                {:state init-state :orientation 0}
                check-pos-fn
                context
                decisions-table))

(defn def-poschecking-fn [width height]
  (fn [[x y]]
    (and (<= x width)
         (<= y height)
         (>= x 0)
         (>= y 0))))

(defn ^:export run [id]
  (let [canvas (.getElementById js/document id)
        width (-> (aget canvas "width") (/ pixel-ratio) int)
        height (-> (aget canvas "height") (/ pixel-ratio) int)
        context (.getContext canvas "2d")
        check-pos-fn (def-poschecking-fn width height)
        decisions-table (rand-nth available-patterns)]
    (log "Running pattern " (:name decisions-table))
    (init-colors width height)
    (run-turmite (rand-int 2)
                 (rand-int 2)
                 (int (/ width 2))
                 (int (/ height 2))
                 check-pos-fn
                 context
                 decisions-table)
    (:name decisions-table)))
