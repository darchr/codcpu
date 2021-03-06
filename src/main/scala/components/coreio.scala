// The IO between the core and the rest of the system

package CODCPU

import chisel3._

class CoreIO extends Bundle {
  val imem = Flipped(new IMemIO)
  val dmem = Flipped(new DMemIO)
}
