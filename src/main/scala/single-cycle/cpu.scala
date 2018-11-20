// This file is where all of the CPU components are assembled into the whole CPU

package CODCPU

import chisel3._
import chisel3.util.Counter

import Common.MemPortIo

/**
 * From Sodor for hooking up memory to the core.
 */
class CoreIo extends Bundle
{
  val imem = new MemPortIo(32)
  val dmem = new MemPortIo(32)
}

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.21
 */
class CPU extends Module {
  val io = IO(new CoreIo())
  io := DontCare

  // All of the structures required
  val pc         = RegInit(0.U)
  val control    = Module(new Control)
  val registers  = Module(new RegisterFile)
  val aluControl = Module(new ALUControl)
  val alu        = Module(new ALU)
  val immGen     = Module(new ImmediateGenerator)
  val pcPlusFour = Module(new Adder)
  val branchAdd  = Module(new Adder)
  val memory     = Module(new SimpleAsyncMemory)
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  memory.io.instPort.address := pc

  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  val instruction = memory.io.instPort.instruction
  val opcode = instruction(6,0)

  control.io.opcode := opcode

  registers.io.readreg1 := instruction(19,15)
  registers.io.readreg2 := instruction(24,20)

  registers.io.writereg := instruction(11,7)
  registers.io.wen      := control.io.regwrite && (registers.io.writereg =/= 0.U)

  aluControl.io.aluop  := control.io.aluop
  aluControl.io.funct7 := instruction(31,25)
  aluControl.io.funct3 := instruction(14,12)

  immGen.io.instruction := instruction
  val imm = immGen.io.sextImm

  val alu_inputy = Mux(control.io.alusrc, imm, registers.io.readdata2)
  alu.io.inputx := registers.io.readdata1
  alu.io.inputy := alu_inputy
  alu.io.operation := aluControl.io.operation

  memory.io.dataPort.address   := alu.io.result
  memory.io.dataPort.writedata := registers.io.readdata2
  memory.io.dataPort.memread   := control.io.memread
  memory.io.dataPort.memwrite  := control.io.memwrite

  val write_data = Mux(control.io.memtoreg, memory.io.dataPort.readdata, alu.io.result)
  registers.io.writedata := write_data

  branchAdd.io.inputx := pc
  branchAdd.io.inputy := imm
  val next_pc = Mux(control.io.branch & alu.io.zero,
                    branchAdd.io.result,
                    pcPlusFour.io.result)

  printf("DASM(%x)\n", instruction)
  printf("Cycle=%d pc=0x%x, r1=%d, r2=%d, rw=%d, daddr=%x, npc=0x%x\n",
         cycleCount,
         pc,
         registers.io.readreg1,
         registers.io.readreg2,
         registers.io.writereg,
         memory.io.dataPort.address,
         next_pc
         )
  printf("                 r1=%x, r2=%x, imm=%x, alu=%x, data=%x, write=%x\n",
         registers.io.readdata1,
         registers.io.readdata2,
         imm,
         alu.io.result,
         memory.io.dataPort.readdata,
         registers.io.writedata
         )

  pc := next_pc

}
