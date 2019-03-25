package uk.ac.cam.swm11.riscvlite;

import java.io.IOException;

class Processor {

  // Storage for the memory and architectural state
  private Memory mem;
  private ArchState archst;

  private Processor(Memory memory, ArchState archState) {
    this.mem = memory;
    this.archst = archState;
  }

  /** Initialise memory and architectural state. */
  static Processor initialize(int memSizeBytes, String progamFilePath, int startPc)
      throws IOException {
    return new Processor(Memory.initialize(memSizeBytes, progamFilePath), new ArchState(startPc));
  }

  ArchState.ExecuteState executeStep() {
    return archst.executeStep(mem);
  }

  /** Dump memory with instructions decoded. */
  void decodeDump(int lowerBound, int upperBound) {
    for (int a = lowerBound; a <= upperBound; a = a + 4) {
      int m = this.mem.load(a);
      System.out.format("0x%04x: 0x%08x %s%n", a, m, DecodedInst.decode(m));
    }
  }

  /** Report on instruction executed. */
  void traceExecutedInstruction() {
    archst.traceExecutedInstruction(mem);
  }
}
