

package CODCPU

import java.nio.file.{Files, Paths}
import java.nio.{ByteBuffer, ByteOrder}

import chisel3._
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import Constants._

class SingleCycleUnitTester(c: CPU, rawfile: String) extends PeekPokeTester(c) {
  def load_memory(filename: String, c: CPU) = {
    val buf = ByteBuffer.wrap(Files.readAllBytes(Paths.get(filename)))
    buf.order(ByteOrder.LITTLE_ENDIAN) // WHY WOULD THIS DEFAULT TO BIG ENDIAN???
    var word = 0
    while(buf.hasRemaining()) {
      val data = buf.getInt()
      poke(c.memory.io.dataPort.writedata, data)
      poke(c.memory.io.dataPort.address, word << 2) // Doing words, not bytes
      poke(c.memory.io.dataPort.memwrite, true)
      step(1)
      word += 1
    }
  }
  load_memory(rawfile, c)
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