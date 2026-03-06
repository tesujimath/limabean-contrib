# example-set-narration

Simplest possible example plugin which overrides the narration field of each transaction with a fixed value.

## Usage

```
plugin "limabean.contrib.plugins.examples.set-narration" "{:narration \"Plugins rule ok!\"}"
```

The config string is optional.

## Description

Every transaction is replaced with one which has the specified narration, with a default value used if no config string is supplied.

## Implementation notes

[Plugin source code](../src/limabean/contrib/plugins/examples/set_narration.clj)
