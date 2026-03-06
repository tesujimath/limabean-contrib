# example-magic-money

Example plugin which inserts transactions.

## Usage

```
plugin "limabean.contrib.plugins.examples.magic-money" "{:units 1000.00M :cur \"USD\" :acc \"Equity:Rich-American-Uncle\"}"
```

The config string is optional.

## Description

After every `open` directive, inserts a transaction that transfers money from the equity account.

Additionally, just before the first `open` directive, a new `open` directive for the magic equity account is inserted.

The amount and account name are given in the config, or defaults are used if the config is omitted.

## Implementation notes

This is an example of a [stateful transducer](https://clojure.org/reference/transducers#_transducers_with_reduction_state).

[Plugin source code](../src/limabean/contrib/plugins/examples/magic_money.clj)
