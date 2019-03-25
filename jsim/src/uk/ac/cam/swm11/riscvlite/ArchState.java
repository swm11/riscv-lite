package uk.ac.cam.swm11.riscvlite;

// Architectural state of the processor
class ArchState {

  enum ExecuteState {
    STOPPED,
    RUNNING
  }

  int pc, nextpc; // current and next program counter values
  int[] rf; // register file

  ArchState(int startPc) {
    this.nextpc = startPc;
    this.pc = -1;
    this.rf = new int[32]; // will contain zeros
  }

  ExecuteState executeStep(Memory mem) {
    // Move onto the next pc
    this.pc = this.nextpc;
    // Ensure register zero is always 0
    this.rf[0] = 0;
    // Fetch and decode instruction
    DecodedInst d = DecodedInst.decode(mem.load(this.pc));
    // By default the nextpc is the next instruction
    this.nextpc = this.pc + 4;
    switch (d.inst) {
      case ADD: // add two registers
        this.rf[d.rd] = this.rf[d.rs1] + this.rf[d.rs2];
        break;
      case ADDI: // add a register and an immediate (i.e. a constant)
        this.rf[d.rd] = this.rf[d.rs1] + d.imm;
        break;
      case AUIPC: // program counter relative immediate
        this.rf[d.rd] = this.pc + d.imm;
        break;
      case LUI: // load upper immediate
        this.rf[d.rd] = d.imm;
        break;
      case BLT: // branch less than
        if (this.rf[d.rs1] < this.rf[d.rs2]) this.nextpc = this.pc + d.imm;
        break;
      case JAL: // jump and link (pc + immediate)
        this.nextpc = this.pc + d.imm;
        this.rf[1] = this.pc + 4; // x1 = ra (return address)
        break;
      case JALR: // jump and link (register + immediate)
        this.nextpc = this.rf[d.rs1] + d.imm;
        this.rf[1] = this.pc + 4; // x1 = ra (return address)
        break;
      case LW: // load word
        this.rf[d.rd] = mem.load(this.rf[d.rs1] + d.imm);
        break;
      case SW: // store word
        mem.store(this.rf[d.rs1] + d.imm, this.rf[d.rs2]);
        break;
      default:
        System.out.format("ERROR: Undefined instruction at pc=0x%08x\n", this.pc);
        this.nextpc = this.pc; // trigger stop condition
    }
    return this.nextpc == this.pc ? ExecuteState.STOPPED : ExecuteState.RUNNING;
  }
}
