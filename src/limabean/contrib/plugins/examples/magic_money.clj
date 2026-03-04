(ns limabean.contrib.plugins.examples.magic-money)

(defn booked-xf
  "Stateful transducer on booked directives to add money to each account which is opened.

  For an explanation of transducers, see https://clojure.org/reference/transducers"
  [{:keys [config options]}]
  (let [acc (or (:acc config) "Equity:Magic")
        units (or (:units config) 100.00M)
        cur (or (:cur config) "NZD")]
    (fn [rf]
      (let [magic-acc-opened (volatile! false)]
        (fn
          ;; init
          ([] (rf))
          ;; completion
          ([result] (rf result))
          ;; step
          ([result d]
           (if (= (:dct d) :open)
             (do (when-not @magic-acc-opened
                   ;; emit an open directive for the magic equity account
                   ;; before the first open
                   (rf result {:date (:date d), :dct :open, :acc acc})
                   (vreset! magic-acc-opened true))
                 ;; emit the original open
                 (rf result d)
                 ;; emit the magic money transaction
                 (rf result
                     {:date (:date d),
                      :dct :txn,
                      :postings [{:acc acc, :units (- units), :cur cur}
                                 {:acc (:acc d),
                                  :units units,
                                  :cur cur,
                                  :payee "magical benefactor"}]}))
             ;; otherwise emit the original directive, whatever it was
             (rf result d))))))))
