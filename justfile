build:
    clojure -T:build jar

test:
    clojure -X:test

refresh-golden-test-output:
    clojure -X:gen-golden
