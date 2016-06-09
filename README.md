# validation-benchmark

## Running The Benchmark

Run the benchmark with the command below, note that this will take a long time:


    $ lein run

Or the quick(er) test with:

    $ lein run -- --mode quick

You can also modify the file at  resources/tests.edn to affect which libs and tests get run.

To display command line help:


    $ lein run -- --help


## Adding More Benchmarks

[tests.edn](https://github.com/muhuk/validation-benchmark/blob/master/resources/tests.edn)

**TBD**


## Adding More Libraries

Currently [these libraries](https://github.com/muhuk/validation-benchmark/tree/master/src/validation_benchmark/lib) have implementations.

**TBD**


## See Also

Related blog posts:

- [Performance Comparison of Annotate, Herbert & Schema](http://blog.muhuk.com/2016/04/18/performance_comparison_of_annotate_herbert_schema.html)
- [Benchmarking Clojure Validation Libraries](http://blog.muhuk.com/2016/03/15/benchmarking_clojure_validation_libraries.html)
- [Performance Cost of Runtime Type Checking](http://blog.muhuk.com/2016/02/23/performance_cost_of_runtime_type_checking.html)


## License

Copyright © 2016 Atamert Ölçgen

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
