package uk.ac.cam.swm11.riscvlite;

// Decoded instruction type
class DecodedInst {
  enum InstClass {
    R_TYPE,
    I_TYPE,
    S_TYPE,
    B_TYPE,
    U_TYPE,
    J_TYPE,
    UNDEFINED
  }

  enum InstT {
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

  InstClass typ; // decoded instruction type
  InstT inst; // decoded instruction
  int imm; // decoded immediate operand
  byte rd, rs1, rs2; // registers
  byte opcode, funct3, funct7;
}
