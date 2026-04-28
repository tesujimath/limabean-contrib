# limabean-contrib

A supplementary package for [limabean](https://github.com/tesujimath/limabean/blob/main/README.md) which welcomes contributions, in particular [plugins](https://github.com/tesujimath/limabean/blob/main/clj/doc/40-plugins.md).

## Plugins

+ [example-magic-money](doc/example-magic-money.md) - stateful transducer example
+ [example-set-narration](doc/example-set-narration.md) - simplest possible example plugin

## Contributing

Contributions of new plugins are very welcome.  Steps to take:

1. Write and test your plugin
2. Add a full description in the [docs](doc), and link to it from the list above
3. Add a test, as below

A plugin is simply a [Clojure transducer](https://clojure.org/reference/transducers).  The function which creates the transducer takes two parameters: the `config` supplied in the beanfile (or `nil` if none), and the `options` extracted from the beanfile, as made available in the REPL as `*options*`.

### Tests

Each plugin has its tests alongside the plugin, and will be found be the test runner.  At least two tests are expected: with and without config.  Additional tests are welcome, but not required.

Each test comprises a Beancount file together with a sibling golden output directory containing expected test output in EDN format.  The test runner will find all such pairs and run the tests.

Running of the tests does require the supplementary excutable `limabean-pod` to be on the path, for parsing of Beancount files.

Golden output files may be re-written using the following command.  Note that `raw-xf-directives.edn` is written only when there are raw plugins.

```
kiri> clojure -X:gen-golden
```

which requires at least limabean 0.4.2

To verify all tests are passing:

```
kiri> clojure -X:test
```

## License

Copyright © 2025-26 Simon Guest, except as otherwise indicated on individual plugins.

Unless otherwise stated, licensed under either of

 * Apache License, Version 2.0
   [LICENSE-APACHE](http://www.apache.org/licenses/LICENSE-2.0)
 * MIT license
   [LICENSE-MIT](http://opensource.org/licenses/MIT)

at your option.

Individual plugins may have different licenses, for example where these are derived works of original Beancount plugins.
