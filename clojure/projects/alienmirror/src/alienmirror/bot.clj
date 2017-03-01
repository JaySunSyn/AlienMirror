(ns alienmirror.bot
  (:gen-class)
  (:require [clojure.string :as s]
            [environ.core :refer [env]]
            [alienmirror.facebook :as fb]
            [alienmirror.googleupload :as gu]
            [alienmirror.api.vision :as vision]
            [clojure.data.json :as json]))

(defn on-message [payload]
  (println "on-message payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        message-text (get-in payload [:message :text])]
    (cond
      (s/includes? (s/lower-case message-text) "help") (fb/send-message sender-id (fb/text-message "Hi there, happy to help :)"))
      (s/includes? (s/lower-case message-text) "image") (fb/send-message sender-id (fb/image-message "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/M101_hires_STScI-PRC2006-10a.jpg/1280px-M101_hires_STScI-PRC2006-10a.jpg"))
      ;;; If no rules apply echo the user's message-text input
      :else (fb/send-message sender-id (fb/text-message message-text)))))

(defn on-postback [payload]
  (println "on-postback payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        postback (get-in payload [:postback :payload])
        referral (get-in payload [:postback :referral :ref])]
    (cond
      (= postback "GET_STARTED") (fb/send-message sender-id (fb/text-message "Welcome =)"))
      :else (fb/send-message sender-id (fb/text-message "Sorry, I don't know how to handle that postback")))))


(def compliments ["These are beautiful eyes!" "I like this nose!" "Wow, those lips!" "Nice ears!"])
(defn on-image [sender-id attachment]
  ;;; see vision.clj:22 and https://cloud.google.com/vision/docs/how-to
  ;;; for further information about the vision api
  (println "happy non senses:")
  (let [vision-response (vision/analyze (get-in attachment [:payload :url])) dummy (println (str vision-response))]

    (cond
      (contains? vision-response :faceAnnotations)
      (let [face-annotations (:faceAnnotations vision-response)
lh_values ["UNKNOWN" "VERY_UNLIKELY" "UNLIKELY" "POSSIBLE" "LIKELY" "VERY_LIKELY"]
lh_keys [:joyLikelihood :sorrowLikelihood :angerLikelihood :surpriseLikelihood :underExposedLikelihood :blurredLikelihood :headwareLikelihood]
]
        ;(fb/send-message sender-id (fb/text-message (rand-nth compliments)))
(println "-------- faceann -----------")
(spit "fan.txt" (json/write-str (str face-annotations))) 
(println "-------- endfaceann -----------")

(fb/send-message sender-id (fb/new-image-message "https://storage.googleapis.com/alienmirror/images/testframealien.png" (:sorrowLikelihood  (first face-annotations)) "This is the subject")))

      (contains? vision-response :labelAnnotations)
      (let [label-annotations (:labelAnnotations vision-response)
            face-annotations (:faceAnnotations vision-response)]
        (let [firstLabel (:description (first label-annotations))]
            ;;(gu/upload-image "xyz" "filename")
(println "-------- joylike -----------")
(println vision-response)
(println "-------- joylikeend -----------")
(fb/send-message sender-id (fb/new-image-message "https://storage.googleapis.com/alienmirror/images/testframealien.png" "unsertitel"  "thesub"))

))
      :else (fb/send-message sender-id (fb/text-message "Uhm, I'm not sure what that is, but its beautiful!")))))

(defn on-audio [sender-id attachment]
  (fb/send-message sender-id (fb/text-message "That sounds beautiful!")))

(defn on-attachments [payload]
  (println "on-attachment payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        attachments (get-in payload [:message :attachments])]
    (let [attachment (first attachments)]
      (cond
        (= (:type attachment) "image") (on-image sender-id attachment)
        (= (:type attachment) "audio") (on-audio sender-id attachment)))))
