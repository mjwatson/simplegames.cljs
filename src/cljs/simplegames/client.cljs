(ns simplegames.client 
  (:require [clojure.browser.repl]))

;; Simple snake implementation
;; ***************************

;; The canvas

(def canvas (-> js/document (.getElementById "canvas")))

(def context (.getContext canvas "2d"))

(def width (.-width canvas))

(def height (.-height canvas))

;; Board

(def background "white")

(def snake-head-colour "blue")

(def snake-tail-colour "green")

(def rabbit-colour "brown")

(def x-max 50)

(def y-max 50)

(def x-scale (/ width x-max))

(def y-scale (/ height y-max))

(def diameter (min x-scale y-scale))

(defn get-pos [x scale]
  (* scale (+ x 0.5)))

(defn pos [[x y]]
  [ (get-pos x x-scale)
    (get-pos y y-scale)])

(def centre (pos [(/ x-max 2) (/ y-max 2)]))

;; The current state of the application
;; Stored in an atom for access via the repl

(def state (atom {}))

(def initial-state {:status :ready
                    :heading [0 1]
                    :snake   '([0 0])
                    :rabbits #{} })

(defn init-state []
  (reset! state initial-state))

; Note disallow sudden reversal of direction since will kill the game.

(defn opposite-direction [{h :heading}]
  (map #(* -1 %) h))

(defn set-heading [xy]
  (when (not= xy (opposite-direction @state))
    (swap! state assoc :heading xy)))

(defn score [state]
  (dec (count (:snake state))))

(defn head [{[h & t] :snake}] h)

(defn tail [{[h & t] :snake}] t)

(defn next-pos [state]
  (map + (head     state)
         (:heading state)))


(defn mod-snake [grow? state]
  (let [change (if grow? identity butlast)]
    (assoc state
      :snake
      (cons (next-pos state)
            (change (:snake state))))))

(defn grow [state]
  (mod-snake true state))

(defn move [ state ]
  (mod-snake false state))

(defn set-status [state status]
  (assoc state :status status))

(defn inbounds [state]
  (let [[x y] (head state)]
    (and (<= 0 x x-max)
         (<= 0 y y-max))))

(defn includes? [x l]
  (some #{x} l))

(defn collision [state]
  (includes? (head state) (tail state)))

(defn end-of-game? [state]
  (or (not (inbounds state))
      (collision state)))

(defn find-collisions [state]
  (set-status state
    (if (end-of-game? state)
      :over
      :play)))

(defn eats? [state]
  ((:rabbits state) (head state)))

(defn eat [state]
  (update-in state [:rabbits] disj (head state)))

(defn new-rabbit
  ([]
     [(rand-int x-max) (rand-int y-max)])
  ([{snake :snake}]
     (first (remove #(includes? % snake) (repeatedly new-rabbit)))))

(defn create-rabbit [state]
  (update-in state [:rabbits] conj (new-rabbit state)))

(defn add-rabbit []
  (swap! state create-rabbit))

(defn update-snake [state]
  (if (eats? state)
    (-> state eat grow create-rabbit find-collisions)
    (-> state move find-collisions)))

(defn update-state []
  (when (= :play (@state :status))
    (swap! state update-snake)))

(defn new-game []
  (-> initial-state
      (set-status :play)
      create-rabbit))

(defn start-game []
  (when (not= :play (@state :status))
    (reset! state (new-game))))

(defn speed [state]
  (let [r  (* 10 (+ 2 (quot (score state) 5)))]
    (/ 1000 r)))


;; Draw the canvas

(defn setColor [context color]
  (set! (.-fillStyle context) color))

(defn clear []
  (doto context
    (setColor background)
    (.fillRect  0 0 width height)))

(defn draw-ball
  ([ colour diameter [x y]]
     (doto context
       (setColor colour)
       .beginPath
       (.arc   x  y diameter 0 (* 2 Math/PI) true)
       .closePath
       .fill )))

(defn draw-all [colour xs]
  (doseq [ x xs ]
    (draw-ball colour diameter (pos x))))

(defn write-text [text]
  (let [[x y] centre]
    (set! (.-fillStyle context) "blue")
    (set! (.-font context) "bold 16px Arial")
    (set! (.-textAlign context) "center")
    (.fillText context text x y)))

(defn draw-board [s]
  (draw-all  rabbit-colour     (:rabbits s))
  (draw-all  snake-tail-colour (:snake s))
  (draw-all snake-head-colour  [(head s)]))

(defn draw-ready [state]
  (write-text "Lets go! Press enter to start."))

(defn draw-over [state]
  (draw-board state)
  (write-text (str  "Well done - you scored " (score state) "!" "\n" "Press enter to start.")))

(defn draw-play [state]
  (draw-board state))

(defn draw! [state]
  (clear)
  (case (:status state)
    :ready (draw-ready state)
    :play  (draw-play state)
    :over  (draw-over state)))

;; Allow arrow keys to shove the ball

(def key-mappings
  {37 [-1 0]
   38 [0 -1]
   39 [1  0]
   40 [0  1]})

(def ENTER 13)

(def foo (atom nil))

(defn on-keydown [e]
  (reset! foo e)
  (if-let [[x y] (key-mappings (.-keyCode e))]
    (do (.preventDefault e)
        (set-heading [x y])
        false)
    (if (= (.-keyCode e) ENTER)
      (start-game))))

(defn register-for-key-events []
  (.addEventListener js/window "keydown" on-keydown))

;; Top level loop

(def running (atom false))

(defn requestAnimation [f t]
  (.setTimeout js/window f t))

(defn run []
  (requestAnimation run (speed @state))
  (when @running
    (update-state)
    (draw! @state)))

(defn start [& _]
  (reset! running true))

(defn stop []
  (reset! running false))

(defn restart []
  (stop)
  (init-state)
  (start))

;; Initialise the whole she-bang.

(defn ^:export init []
  (register-for-key-events)
  (restart)
  (run))
