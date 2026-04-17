(ns beancount-reds-plugins.opengroup.opengroup
  (:require [clojure.string :as str]))

;; TODO ^{:private true}
(def DEFAULT-RULES
  {"cash_and_fees"
     [; Open cash and fees accounts
      #"(?<root>[^:]*):(?<subroot>[^:]*):(?<taxability>[^:]*):(?<accountname>.*)"
      [["{{f_acct}}:{{f_ticker}}" "{{f_opcurr}}"]
       ["Expenses:Fees-and-Charges:Brokerage-Fees:{{taxability}}:{{accountname}}"
        "{{f_opcurr}}"]]],
   "commodity_leaves_income"
     [; Open common set of investment accounts with commodity leaves
      #"(?<root>[^:]*):(?<subroot>[^:]*):(?<taxability>[^:]*):(?<accountname>.*)"
      [["Income:{{subroot}}:{{taxability}}:Dividends:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Interest:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Capital-Gains:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]]],
   "commodity_leaves_income_and_asset"
     [; Open commodity_leaves_income + asset account for the ticker
      #"(?<root>[^:]*):(?<subroot>[^:]*):(?<taxability>[^:]*):(?<accountname>.*)"
      [["{{f_acct}}:{{f_ticker}}" "{{f_ticker}}"]
       ["Income:{{subroot}}:{{taxability}}:Dividends:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Interest:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Capital-Gains:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]]],
   "commodity_leaves_cgdists" ; Open capital gains distributions accounts
     [#"(?<root>[^:]*):(?<subroot>[^:]*):(?<taxability>[^:]*):(?<accountname>.*)"
      [["Income:{{subroot}}:{{taxability}}:Capital-Gains-Distributions:Long:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]
       ["Income:{{subroot}}:{{taxability}}:Capital-Gains-Distributions:Short:{{accountname}}:{{f_ticker}}"
        "{{f_opcurr}}"]]]})

(defn opengroup_meta_rule_leaves
  "Return [rule, [leaves]] for any metadata strings whose name starts opengroup_"
  [dct]
  (keep (fn [[k v]]
          (let [k-name (name k)
                v-string (:string v)]
            (when (and (str/starts-with? k-name "opengroup_") v-string)
              [(str/replace k-name "opengroup_" "")
               (str/split v-string #",")])))
        (:metadata dct)))

(defn warn [& args] (binding [*out* *err*] (apply println args)))

(defn re-matched-groups
  [m]
  (and m (into {} (map (fn [[name i]] [name (.group m i)]) (.namedGroups m)))))

(defn run-rules
  [rules rulename f_acct f_ticker f_opcurr entry]
  (let [[rule inserts] (get rules rulename)]
    (if rule
      (let [m (re-matcher rule f_acct)] (and (re-find m) m))
      (warn "rule" rulename "not found"))))

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
        op-currency (or (first (:operating-currency options)) "USD")]
    (fn [rf]
      (fn
        ;; init
        ([] (rf))
        ;; completion
        ([result] (rf result))
        ;; step
        ([result dct]
         (rf result
             (let [result' (rf result dct)]
               (if (= (:dct dct) :open)
                 (reduce (fn [result'' [rule leaves]])
                   result'
                   (opengroup_meta_rule_leaves dct))
                 result'))))))))
