package uk.ac.cam.swm11.riscvlite;

// Architectural state of the processor
class ArchState {
  int pc, nextpc; // current and next program counter values
  int[] rf; // register file

  ArchState(int startPc) {
    this.nextpc = startPc;
    this.pc = -1;
    this.rf = new int[32]; // will contain zeros
  }
}
