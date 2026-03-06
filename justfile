build:
    clojure -T:build jar

test:
    clojure -X:test

create-missing-plugin-tests:
    clojure -T:build create-plugin-tests

update-plugin-tests:
    clojure -T:build create-plugin-tests '{:force true}'
