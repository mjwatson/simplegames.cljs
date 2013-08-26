(ns simplegames.server
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as enlive]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.core :refer (GET defroutes)]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [clojure.java.io :as io])
  (:gen-class))

;; Utility method to start the brepl


(defn start-repl []
  (let [repl-env (reset! cemerick.austin.repls/browser-repl-env
                         (cemerick.austin/repl-env))]
    (cemerick.austin.repls/cljs-repl repl-env)))

;; Horrible hack needed to make the page connect to the austin brepl.
;; Basically appends the necessary javascript to the end of the main page
;; Currently reloads on each request to aid development work-flow

(defn page []
  ((enlive/template 
     "public/index.html"
     []
     [:body] (enlive/append 
               (enlive/html 
                 [:script (browser-connected-repl-js)])))))

;; The routes - pretty simple for now.

(defroutes app-routes
  (GET "/" [] (response/redirect "index.html"))
  (GET "/index.html" req (page))
  (route/resources "/")
  (route/not-found (str "Page not found" "!")))

;; Start the server via lein run, lein ring server or from a repl.

(def handler
  (handler/site app-routes))

(defn start [{:keys [start join?] :as options}]
  (let [ server (jetty/run-jetty #'app-routes options)]
    server))

(defn run []
  (start {:port 8080 :join? false}))

(defn -main [port & args]
  (start {:port (Integer. port) :join? true}))

