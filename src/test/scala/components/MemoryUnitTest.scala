
package CODCPU


import java.nio.{ByteBuffer, ByteOrder}
import java.nio.file.{Files, Paths}

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class MemoryUnitTester(c: SimpleAsyncMemory) extends PeekPokeTester(c) {
  def load_memory(filename: String, memory: SimpleAsyncMemory) = {
    val buf = ByteBuffer.wrap(Files.readAllBytes(Paths.get(filename)))
    buf.order(ByteOrder.LITTLE_ENDIAN) // WHY WOULD THIS DEFAULT TO BIG ENDIAN???
    val lim = buf.limit()
    println(s"found $lim bytes")
    var word = 0
    while(buf.hasRemaining()) {
      val data = buf.getInt()
      println(s"$data")
      poke(memory.io.dataPort.writedata, data)
      poke(memory.io.dataPort.address, word << 2) // Doing words, not bytes
      poke(memory.io.dataPort.memwrite, true)
      step(1)
      word += 1
    }
  }
  load_memory("tests/add1.raw", c)
  poke(c.io.instPort.address, 0)
  expect(c.io.instPort.instruction, 0x00500333)
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