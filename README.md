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

Each plugin has its tests in a subdirectory of [plugin-tests](plugin-tests).  At least two tests are expected: with and without config.  Additional tests are welcome, but not required.

To avoid a dependency on the limabean-pod Rust binary from limabean, test data is created offline by running a local copy of `limabean-pod`, and formatted uniformly using `zprint`.  Each test, therefore, is created from a `.beancount` file.  To create the necessary test input and golden output files, run the following command, which required `limabean-pod` to be on the path.

```
kiri> clojure -T:build create-plugin-tests
```

To force all test input and golden output files to be regenerated:
```
clojure -T:build create-plugin-tests '{:force true}'
```

For this to work smoothly with correct formatting of Java `LocalDate` objects, use limabean at least version 0.3.2.

To verify all tests are passing:

```
kiri> clojure -X:test
```

## Booked vs Raw plugins

For now, only plugins which operate on the fully resolved booked directives are supported.  These implement the function `booked-xf`.

Work is underway to add support for plugins which operate on the raw directives before validation.

## License

Copyright © 2025-26 Simon Guest

Licensed under either of

 * Apache License, Version 2.0
   [LICENSE-APACHE](http://www.apache.org/licenses/LICENSE-2.0)
 * MIT license
   [LICENSE-MIT](http://opensource.org/licenses/MIT)

at your option.
