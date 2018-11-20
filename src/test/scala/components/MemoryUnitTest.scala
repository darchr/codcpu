
package CODCPU


import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class MemoryUnitTester(c: SimpleAsyncMemory) extends PeekPokeTester(c) {
  poke(c.io.dataPort.address, 0)
  poke(c.io.dataPort.memread, 1)
  expect(c.io.dataPort.readdata, 0x00500333)
}


/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.MemoryTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.MemoryTester'
  * }}}
  */
class MemoryTester extends ChiselFlatSpec {
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "SimpleAsyncMemory" should s"save written values (with $backendName)" in {
      Driver(() => new SimpleAsyncMemory, backendName) {
        c => new MemoryUnitTester(c)
      } should be (true)
    }
  }
}