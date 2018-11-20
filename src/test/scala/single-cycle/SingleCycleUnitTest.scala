

package CODCPU

import java.nio.file.{Files, Paths}
import java.nio.ByteBuffer

import chisel3._
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import Constants._

class SingleCycleUnitTester(c: CPU, rawfile: String) extends PeekPokeTester(c) {
  def load_memory(filename: String, memory: SimpleAsyncMemory) = {
    val buf = ByteBuffer.wrap(Files.readAllBytes(Paths.get(filename)))
    val lim = buf.limit()
    println(s"found $lim bytes")
    var word = 0
    while(buf.hasRemaining()) {
      val data = buf.getInt()
      println(s"$data")
      //poke(memory.io.dataPort.writedata, data)
      //poke(memory.io.dataPort.address, word << 2) // Doing words, not bytes
      //poke(memory.io.dataPort.memwrite, 1)
      step(1)
      word += 1
    }
  }
  load_memory(rawfile, c.memory)
  poke(c.memory.io.instPort.address, 0)
  expect(c.memory.io.instPort.instruction, 0x00500333)

}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.SingleCycle
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.SingleCycle'
  * }}}
  */
class SingleCycleTester extends ChiselFlatSpec {
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("treadle", "verilator")
  }
  else {
    Array("treadle")
  }
  for ( backendName <- backendNames ) {
    "SingleCycle" should s"run correctly (with $backendName)" in {
      Driver.execute(Array("--is-verbose", "--backend-name", backendName), () => new CPU) {
        c => new SingleCycleUnitTester(c, "tests/add1.raw")
      } should be (true)
    }
  }
}