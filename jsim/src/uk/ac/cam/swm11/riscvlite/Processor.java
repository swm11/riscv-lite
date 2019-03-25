package uk.ac.cam.swm11.riscvlite;

import java.io.IOException;

class Processor {
  public enum ExecuteState {
    STOPPED,
    RUNNING
  }

  private static final String[] REG_AB_INAME_STR = {
    "zero", "ra", "sp", "gp",
    "tp", "t0", "t1", "t2",
    "fp", "s1", "a0", "a1",
    "a2", "a3", "a4", "a5",
    "a6", "a7", "s2", "s3",
    "s4", "s5", "s6", "s7",
    "s8", "s9", "s10", "s11",
    "t3", "t4", "t5", "t6"
  };

  // Architectural state of the processor
  private static class ArchState {
    int pc, nextpc; // current and next program counter values
    int[] rf; // register file

    ArchState(int startPc) {
      this.nextpc = startPc;
      this.pc = -1;
      this.rf = new int[32]; // will contain zeros
    }
  }

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

  ExecuteState executeStep() {
    // Move onto the next pc
    archst.pc = archst.nextpc;
    // Ensure register zero is always 0
    archst.rf[0] = 0;
    // Fetch and decode instruction
    DecodedInst d = DecodedInst.decode(this.mem.load(archst.pc));
    // By default the nextpc is the next instruction
    archst.nextpc = archst.pc + 4;
    switch (d.inst) {
      case ADD: // add two registers
        archst.rf[d.rd] = archst.rf[d.rs1] + archst.rf[d.rs2];
        break;
      case ADDI: // add a register and an immediate (i.e. a constant)
        archst.rf[d.rd] = archst.rf[d.rs1] + d.imm;
        break;
      case AUIPC: // program counter relative immediate
        archst.rf[d.rd] = archst.pc + d.imm;
        break;
      case LUI: // load upper immediate
        archst.rf[d.rd] = d.imm;
        break;
      case BLT: // branch less than
        if (archst.rf[d.rs1] < archst.rf[d.rs2]) archst.nextpc = archst.pc + d.imm;
        break;
      case JAL: // jump and link (pc + immediate)
        archst.nextpc = archst.pc + d.imm;
        archst.rf[1] = archst.pc + 4; // x1 = ra (return address)
        break;
      case JALR: // jump and link (register + immediate)
        archst.nextpc = archst.rf[d.rs1] + d.imm;
        archst.rf[1] = archst.pc + 4; // x1 = ra (return address)
        break;
      case LW: // load word
        archst.rf[d.rd] = this.mem.load(archst.rf[d.rs1] + d.imm);
        break;
      case SW: // store word
        this.mem.store(archst.rf[d.rs1] + d.imm, archst.rf[d.rs2]);
        break;
      default:
        System.out.format("ERROR: Undefined instruction at pc=0x%08x\n", archst.pc);
        archst.nextpc = archst.pc; // trigger stop condition
    }

    return archst.nextpc == archst.pc ? ExecuteState.STOPPED : ExecuteState.RUNNING;
  }

  /** Dump memory with instructions decoded. */
  void decodedump(int lowerBound, int upperBound) {
    int a, m;
    for (a = lowerBound; a <= upperBound; a = a + 4) {
      m = this.mem.load(a);
      DecodedInst d = DecodedInst.decode(m);
      System.out.format(
          "0x%04x: 0x%08x opcode=%s typ=%-5s inst=%-5s rd=%-4s rs1=%-4s rs2=%-4s imm=0x%08x=%d\n",
          a,
          m,
          String.format("%7s", Integer.toBinaryString(d.opcode))
              .replace(' ', '0'), // nasty hack to get exaclty 7 binary digits
          d.typ.name(),
          d.inst.name(),
          REG_AB_INAME_STR[d.rd],
          REG_AB_INAME_STR[d.rs1],
          REG_AB_INAME_STR[d.rs2],
          d.imm,
          d.imm);
    }
  }

  /** Report on instruction executed. */
  void traceExecutedInstruction() {
    DecodedInst d = DecodedInst.decode(this.mem.load(archst.pc)); // fetch and decode instruction
    System.out.format(
        "pc=0x%08x inst=%5s rd=x%02d=%4s=%-8d rs1=%4s=%-8d rs2=%4s=%-8d imm=0x%08x=%d\n",
        archst.pc,
        d.inst.name(),
        d.rd,
        REG_AB_INAME_STR[d.rd],
        archst.rf[d.rd],
        REG_AB_INAME_STR[d.rs1],
        archst.rf[d.rs1],
        REG_AB_INAME_STR[d.rs2],
        archst.rf[d.rs2],
        d.imm,
        d.imm);
    if (archst.nextpc != archst.pc + 4) {
      System.out.format(
          "--------------------jump: 0x%08x->0x%08x --------------------\n",
          archst.pc, archst.nextpc);
    }
    if (archst.nextpc == archst.pc) {
      System.out.format("--------------------STOPPED--------------------\nRegister map:\n");
      for (int r = 0; r < 32; r++) {
        System.out.format(
            "  x%02d = %4s = 0x%08x = %d\n", r, REG_AB_INAME_STR[r], archst.rf[r], archst.rf[r]);
      }
    }
  }
}
