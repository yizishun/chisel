// SPDX-License-Identifier: Apache-2.0

package chiselTests
import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import chisel3.simulator.stimulus.RunUntilFinished
import org.scalatest.propspec.AnyPropSpec

class Coord extends Bundle {
  val x = UInt(32.W)
  val y = UInt(32.W)
}

class BundleWire(n: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(new Coord)
    val outs = Output(Vec(n, new Coord))
  })
  val coords = Wire(Vec(n, new Coord))
  for (i <- 0 until n) {
    coords(i) := io.in
    io.outs(i) := coords(i)
  }
}

class BundleToUnitTester extends Module {
  val bundle1 = Wire(new Bundle {
    val a = UInt(4.W)
    val b = UInt(4.W)
  })
  val bundle2 = Wire(new Bundle {
    val a = UInt(2.W)
    val b = UInt(6.W)
  })

  // 0b00011011 split as 0001 1011 and as 00 011011
  bundle1.a := 1.U
  bundle1.b := 11.U
  bundle2.a := 0.U
  bundle2.b := 27.U

  assert(bundle1.asUInt === bundle2.asUInt)

  stop()
}

class BundleWireTester(n: Int, x: Int, y: Int) extends Module {
  val dut = Module(new BundleWire(n))
  dut.io.in.x := x.asUInt
  dut.io.in.y := y.asUInt
  for (elt <- dut.io.outs) {
    assert(elt.x === x.asUInt)
    assert(elt.y === y.asUInt)
  }
  stop()
}

class BundleWireSpec extends AnyPropSpec with PropertyUtils with ChiselSim {

  property("All vec elems should match the inputs") {
    forAll(vecSizes, safeUInts, safeUInts) { (n: Int, x: Int, y: Int) =>
      simulate { new BundleWireTester(n, x, y) }(RunUntilFinished(3))
    }
  }
}

class BundleToUIntSpec extends AnyPropSpec with ChiselSim {
  property("Bundles with same data but different, underlying elements should compare as UInt") {
    simulate(new BundleToUnitTester)(RunUntilFinished(3))
  }
}
