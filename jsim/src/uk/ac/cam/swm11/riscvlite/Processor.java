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

  private enum InstClass {
    R_TYPE,
    I_TYPE,
    S_TYPE,
    B_TYPE,
    U_TYPE,
    J_TYPE,
    UNDEFINED
  }

  private enum InstT {
    UDEF,
    ADD,
    ADDI,
    AUIPC,
    BLT,
    J,
    JAL,
    JALR,
    LW,
    LUI,
    MV,
    SW
  }

  // Decoded instruction type
  private class DecodedInst {
    InstClass typ; // decoded instruction type
    InstT inst; // decoded instruction
    int imm; // decoded immediate operand
    byte rd, rs1, rs2; // registers
    byte opcode, funct3, funct7;
  }

  // Architectural state of the processor
  private class ArchState {
    int pc, nextpc; // current and next program counter values
    int[] rf; // register file
  }

  // Storage for the memory and architectural state
  private Memory mem;
  private ArchState archst;

  /** Initialise memory and architectural state. */
  void processor(int memsizebytes, String programfilepath, int startPC) throws IOException {
    this.mem = new Memory();
    this.mem.memory(memsizebytes, programfilepath);
    archst = new ArchState();
    archst.nextpc = startPC;
    archst.pc = -1; // value should never be looked at, so set to an invalid value
    archst.rf = new int[32];
    for (int r = 0; r < 32; r++) {
      archst.rf[r] = 0;
    }
  }

  /** Decode an instruction. */
  private DecodedInst decode(int inst) {
    DecodedInst d = new DecodedInst();
    // Decode the fixed fields even if they are not needed for a particular instruction
    d.opcode = BitExtract.bitExtractByte(inst, 0, 6);
    d.rd = BitExtract.bitExtractByte(inst, 7, 11);
    d.rs1 = BitExtract.bitExtractByte(inst, 15, 19);
    d.rs2 = BitExtract.bitExtractByte(inst, 20, 24);
    d.funct3 = BitExtract.bitExtractByte(inst, 12, 14);
    d.funct7 = BitExtract.bitExtractByte(inst, 25, 31);
    switch (d.opcode) {
      case 0b0110011: // R-type instructions (e.g. ADD)
        d.typ = InstClass.R_TYPE;
        d.imm = 0;
        switch ((d.funct7 << 3) | d.funct3) {
          case ((0b000000 << 3) | 0b000):
            d.inst = InstT.ADD;
            break;
          default:
            d.inst = InstT.UDEF;
        }
        break;

      case 0b0010011: // I-type instructions (e.g. ADDI)
      case 0b0000011: // also load instructions
      case 0b1100111: // also JALR
        d.typ = InstClass.I_TYPE;
        d.imm = BitExtract.bitExtractSignedInt(inst, 20, 31);
        switch ((d.opcode << 3) | d.funct3) {
          case (0b0010011 << 3) | 0b000:
            d.inst = InstT.ADDI;
            break;
          case (0b0000011 << 3) | 0b010:
            d.inst = InstT.LW;
            break;
          case (0b1100111 << 3) | 0b000:
            d.inst = InstT.JALR;
            break;
          default:
            d.inst = InstT.UDEF;
        }
        break;
      case 0b0100011: // S-type (store instructions)
        d.typ = InstClass.S_TYPE;
        d.imm = (BitExtract.bitExtractSignedInt(inst, 25, 31) << 5) | d.rd;
        switch (d.funct3) {
          case 0b010:
            d.inst = InstT.SW;
            break;
          default:
            d.inst = InstT.UDEF;
        }
        break;
      case 0b0010111: // AUIPC
        d.typ = InstClass.U_TYPE;
        d.imm = BitExtract.bitExtractInt(inst, 12, 31) << 12;
        d.inst = InstT.AUIPC;
        break;
      case 0b0110111: // LUI
        d.typ = InstClass.U_TYPE;
        d.imm = BitExtract.bitExtractInt(inst, 12, 31) << 12;
        d.inst = InstT.LUI;
        break;
      case 0b1101111: // JAL (J-type)
        d.typ = InstClass.J_TYPE;
        d.imm =
            (BitExtract.bitExtractInt(inst, 21, 30) << 1)
                | (BitExtract.bitExtractInt(inst, 20, 20) << 11)
                | (BitExtract.bitExtractInt(inst, 12, 19) << 12)
                | (BitExtract.bitExtractSignedInt(inst, 31, 31) << 20);
        d.inst = InstT.JAL;
        break;
        //  B-type instrucitons
      case 0b1100011: // conditional branches
        d.typ = InstClass.B_TYPE;
        d.imm =
            BitExtract.bitExtractInt(inst, 8, 11) << 1
                | BitExtract.bitExtractInt(inst, 25, 30) << 5
                | BitExtract.bitExtractInt(inst, 7, 7) << 11
                | BitExtract.bitExtractSignedInt(inst, 31, 31) << 12;
        switch (d.funct3) {
          case 0b100:
            d.inst = InstT.BLT;
            break;
          default:
            d.inst = InstT.UDEF;
        }
        break;

      default:
        d.typ = InstClass.UNDEFINED;
        d.inst = InstT.UDEF;
        d.imm = 0;
    }

    return d;
  }

  ExecuteState executeStep() {
    DecodedInst d;
    // Move onto the next pc
    archst.pc = archst.nextpc;
    // Ensure register zero is always 0
    archst.rf[0] = 0;
    // Fetch and decode instruction
    d = decode(this.mem.load(archst.pc));
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
  void decodedump(Memory mem, int lowerBound, int upperBound) {
    int a, m;
    for (a = lowerBound; a <= upperBound; a = a + 4) {
      m = this.mem.load(a);
      DecodedInst d = decode(m);
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
    DecodedInst d;
    d = decode(this.mem.load(archst.pc)); // fetch and decode instruction
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
