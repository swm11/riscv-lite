package uk.ac.cam.swm11.riscvlite;

import java.io.IOException;

class JSim {
  public static void main(String[] args) {
    Memory mem = new Memory();
    Processor.ExecuteState ps;
    Processor proc = new Processor();
    try {
      // initialise processor and load program binary
      proc.processor(65 * 1024, "../fib/build/mem.bin", 0);
    } catch (IOException e) {
      System.out.format("ERROR: Failed to read binary initialisation file.\n");
    }
    System.out.format("Decoded dump of the initial memory:\n");
    proc.decodedump(mem, 0, 50 * 4);
    // step through execution until the stop condition is met
    do {
      ps = proc.executeStep();
      proc.traceExecutedInstruction();
    } while (ps == Processor.ExecuteState.RUNNING);
  }
}
