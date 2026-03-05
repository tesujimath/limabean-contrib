# limabean-contrib

A supplementary package for [limabean](https://github.com/tesujimath/limabean/blob/main/README.md) which welcomes contributions, in particular [plugins](https://github.com/tesujimath/limabean/blob/main/clj/doc/40-plugins.md).

## Contributing

Contributions of new plugins are very welcome.  Please provide a test for each, ideally with and without config.

### Tests

Each plugin has its tests in a subdirectory of [plugin-tests](plugin-tests).  At least two tests are expected: with and without config.  Additional tests are welcome, but not required.

To avoid a dependency on the limabean-pod Rust binary from limabean, test data is created offline by running a local copy of `limabean-pod`, and formatted uniformly using `zprint`.  Each test, therefore, should be created from a `.beancount` file, for example:

```
kiri> limabean-pod book -f edn ./plugin-tests/example-magic-money/with-config.beancount | zprint >./plugin-tests/example-magic-money/with-config.edn
```

Then, to generate the golden output:

```
kiri> limabean --beanfile ./plugin-tests/example-magic-money/with-config.beancount --eval '(println *directives*)' | zprint >./plugin-tests/example-magic-money/with-config.golden/directives.edn
```

For this to work smoothly with correct formatting of Java `LocalDate` objects, use limabean at least version 0.3.2.

With apologies that this is currently rather cumbersome.

To verify all tests are passing:

```
kiri> clojure -X:test
```

## License

Copyright © 2025-26 Simon Guest

Licensed under either of

 * Apache License, Version 2.0
   [LICENSE-APACHE](http://www.apache.org/licenses/LICENSE-2.0)
 * MIT license
   [LICENSE-MIT](http://opensource.org/licenses/MIT)

at your option.
