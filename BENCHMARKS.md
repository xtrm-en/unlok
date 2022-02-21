# benchmarking
**Note:** These statistics are all based on calculations according to
[benchmarks.kt](https://github.com/xtrm-en/unlok/blob/trunk/src/test/kotlin/benchmarks.kt).
It also means that these times are the times recorded for `n=10000000` iterations.

**Note:** These statistics are based on version **0.2.0** of **Unlok**, they may not be accurate to the main branch.

| Task              	 | Unlok (ms) 	 | Reflection (ms) 	 |
|---------------------|--------------|-------------------|
| Accessor Creation 	 | 252        	 | 667             	 |
| Getter            	 | 90         	 | 34              	 |
| Setter            	 | 11         	 | 41              	 |
| Final Setter      	 | 66         	 | 41              	 |
