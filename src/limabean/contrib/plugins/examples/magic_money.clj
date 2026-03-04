(ns limabean.contrib.plugins.examples.magic-money)

(defn booked-xf
  "Stateful transducer on booked directives to add money to each account which is opened.

  For an explanation of transducers, see https://clojure.org/reference/transducers"
  [{:keys [config options]}]
  (let [acc (or (:acc config) "Equity:Magic")
        units (or (:units config) 100.00M)
        cur (or (:cur config) "NZD")]
    (fn [rf]
      (let [state (volatile! {:acc acc})]
        (fn
          ;; init
          ([] (rf))
          ;; completion
          ([result] (rf result))
          ;; step
          ([result d]
           (if (= (:dct d) :open)
             (do (when (:acc @state)
                   ;; open magic equity account on first open
                   (rf result {:date (:date d), :dct :open, :acc acc})
                   (vreset! state (dissoc @state :acc)))
                 ;; original open
                 (rf result d)
                 ;; magic money transaction
                 (rf result
                     {:date (:date d),
                      :dct :txn,
                      :postings [{:acc acc, :units (- units), :cur cur}
                                 {:acc (:acc d),
                                  :units units,
                                  :cur cur,
                                  :payee "magical benefactor"}]}))
             (rf result d))))))))
