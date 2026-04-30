(ns beancount-reds-plugins.opengroup.opengroup
  (:require [clojure.string :as str]
            [limabean.plugin :as plugin]
            [selmer.parser :as selmer]))

(def ^{:private true} DEFAULT-RULES
  {"cash_and_fees"
     [; Open cash and fees accounts
      #"(?<root>[^:]*):(?<subroot>[^:]*):(?<taxability>[^:]*):(?<accountname>.*)"
      [["{{f_acct}}:{{f_ticker}}" "{{f_opcurr}}"]
       ["Expenses:Fees-and-Charges:Brokerage-Fees:{{taxability}}:{{accountname}}"
        "{{f_opcurr}}"]]],
   "commodity_leaves_income"
     [; Open common set of investment accounts with commodity leaves
      #"(?<root>[^:]*):(?<subroot>[^:]*):(?<taxability>[^:]*):(?<accountname>.*)"
      [["Income:{{subroot}}:{{taxability}}:Capital-Gains:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Dividends:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Interest:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]]],
   "commodity_leaves_income_and_asset"
     [; Open commodity_leaves_income + asset account for the ticker
      #"(?<root>[^:]*):(?<subroot>[^:]*):(?<taxability>[^:]*):(?<accountname>.*)"
      [["{{f_acct}}:{{f_ticker}}" "{{f_ticker}}"]
       ["Income:{{subroot}}:{{taxability}}:Capital-Gains:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Dividends:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Interest:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]]],
   "commodity_leaves_cgdists" ; Open capital gains distributions accounts
     [#"(?<root>[^:]*):(?<subroot>[^:]*):(?<taxability>[^:]*):(?<accountname>.*)"
      [["Income:{{subroot}}:{{taxability}}:Capital-Gains-Distributions:Long:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Capital-Gains-Distributions:Short:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]]]})

(defn- get-meta-rule-leaves
  "Return [rulename [leaves]] for any metadata strings whose name starts opengroup_"
  [dct]
  (keep (fn [[k v]]
          (let [k-name (name k)
                v-string (:string v)]
            (when (and (str/starts-with? k-name "opengroup_") v-string)
              [(str/replace k-name "opengroup_" "")
               (str/split v-string #",")])))
        (:metadata dct)))

(defn- re-matched-groups
  [m]
  (and m (into {} (map (fn [[name i]] [name (.group m i)]) (.namedGroups m)))))

(defn- run-rule
  [rules source rulename dct ticker op-cur]
  (let [acc (:acc dct)
        [rule inserts] (get rules rulename)]
    (if rule
      (let [m (re-matcher rule acc)]
        (and (re-find m)
             (let [g (assoc (update-keys (re-matched-groups m) keyword)
                       :f_acct acc
                       :f_ticker ticker
                       :f_opcurr op-cur)]
               (map (fn [insert]
                      (let [[acc cur] (map #(selmer/render % g) insert)]
                        (assoc (select-keys dct [:dct :date])
                          :acc acc
                          :currencies #{cur}
                          :metadata {:auto nil})))
                 inserts))))
      (plugin/error! dct (str "rule " rulename " not found in " source)))))

(defn- run-rule-for-leaves
  [rules source [rulename leaves] dct op-cur]
  (mapcat #(run-rule rules source rulename dct % op-cur) leaves))

(defn- run-rules-for-group
  [rules source group dct op-cur]
  (mapcat #(run-rule-for-leaves rules source % dct op-cur) group))

(defn raw-xf
  "Port from original plugin in Python with the same license, GPL 3.0.

  https://github.com/redstreet/beancount_reds_plugins/tree/main/beancount_reds_plugins/opengroup

  Differences:
  - regexes use e.g. ?<root> instead of ?P<root>, and the Clojure regex reader prefix #
  - named capture groups cannot contain underscores
  - template strings use double braces, mustache style, rather than single brace Python f-string style
  "
  [{:keys [config options]}]
  (let [rules (or config DEFAULT-RULES)
        source (if config "plugin config" "default rules")
        op-cur (or (first (:operating-currency options)) "USD")]
    (fn [rf]
      (fn
        ;; init
        ([] (rf))
        ;; completion
        ([result] (rf result))
        ;; step
        ([result dct]
         (let [result' (rf result dct)]
           (if (= (:dct dct) :open)
             (reduce rf
               result'
               (run-rules-for-group rules
                                    source
                                    (get-meta-rule-leaves dct)
                                    dct
                                    op-cur))
             result')))))))
