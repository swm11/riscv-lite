# riscv-lite
Versions of RISC-V that only execute the fib() function.

## Directory structure:
- fib: contains the fibonacci code
- csim: simulator written in C
- jsim: simulator written in Java
- vsim: simulator written in SystemVerilog (currently does not support memory operations).
- ecad-labs: simulator built on SystemVerilog with a more sophisticated core.

## Build instructions:
For the java simulator, execute the following commands:
```sh
$ cd jsim
$ make
$ make run
```

For the C simulator, execute the following commands:
```sh
$ cd csim
$ make
$ ./csim
```

The vsim simulator does not currently support memory operations, which causes runtime errors. To build it, execute the following commands:
```sh
$ cd vsim
$ ./runsim.sh
```

For the ecad-labs simulator, execute the following commands:
```sh
$ cd ecad-labs/clarvi
$ vsim -c -do fib_test.do > run.log
$ grep "f0000000" run.log #This will find the magic output store.
```
