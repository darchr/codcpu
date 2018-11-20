# These are things I'd like to improve about this

## General to do

[ ] Package better for students.
[ ] Add better way to exit from my emulator. I think we need to call a syscall (ecall?) to get the SimDTM to pick up on it and exit cleanly. See `RVTEST_PASS` `in riscv-tools/riscv-tests/env/p/riscv_test.h`
[ ] Add an option to the emulator and tester to output the tracefile. Make it a command line option instead of build time.
[ ] A better pipeline viewer

## Very important things!
[ ] Fix everything so it is loaded at 0x80000000 instead of 0x8000000. Oops!

## Ideas for restructuring for assignments

- [ ] Start with just the hazard detection unit and detect all hazards instead of forwarding

## Minor modification

- [ ] Update all names of signals to be `<stage>_<name>`
- [ ] Add documentation about using vals in pipelined CPU

## Testing

- I'd like to have a less hacky way to read and write the registers
- Related, improving the debug interface would be nice.
- Add a way to check memory values

### Tests to add

- [x] add test that "writes" register 0
- [x] beq test
- [x] sub test
- [x] and test
- [x] or test
- [x] Forwarding tests
  - I want to add a test that accumulates lots of registers into x1, for instance
- [ ] Unit test for the forwarding logic
- [ ] Unit test for the control logic

## Things to think about

- Other forwarding paths

## Documentation to add

- [ ] All structures in components need to have details about their I/O

# Some notes on improving testing

I've tried to use the `loadMemoryFromFile` function for debugging.
However, the file must be specified when the memory is created, which is causing me a bit of a headache since I can't specify the file when I'm creating the test.
Also, this requires using the master on Chisel, which I would rather not do.

I also can't seem to poke a register in the register file, which isn't shocking, but it's a little annoying.

So, I think the solution is to do a setup phase where I "load" the memory with the data from the file, and I also update the registers to be the right values.
Then, I can "reset" the CPU by setting the PC back to 0 and then running.