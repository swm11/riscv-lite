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

  private static class FixedFields {
    final byte rd, rs1, rs2; // registers
    final byte opcode, funct3, funct7;

    FixedFields(int inst) {
      this.opcode = BitExtract.bitExtractByte(inst, 0, 6);
      this.rd = BitExtract.bitExtractByte(inst, 7, 11);
      this.rs1 = BitExtract.bitExtractByte(inst, 15, 19);
      this.rs2 = BitExtract.bitExtractByte(inst, 20, 24);
      this.funct3 = BitExtract.bitExtractByte(inst, 12, 14);
      this.funct7 = BitExtract.bitExtractByte(inst, 25, 31);
    }
  }

  final InstClass typ; // decoded instruction type
  final InstT inst; // decoded instruction
  final int imm; // decoded immediate operand
  final byte rd, rs1, rs2; // registers
  final byte opcode, funct3, funct7;

  private DecodedInst(FixedFields fixedFields, InstClass typ, InstT inst) {
    this(fixedFields, typ, inst, 0);
  }

  private DecodedInst(FixedFields fixedFields, InstClass typ, InstT inst, int imm) {
    this.typ = typ;
    this.inst = inst;
    this.imm = imm;
    this.rd = fixedFields.rd;
    this.rs1 = fixedFields.rs1;
    this.rs2 = fixedFields.rs2;
    this.opcode = fixedFields.opcode;
    this.funct3 = fixedFields.funct3;
    this.funct7 = fixedFields.funct7;
  }

  /** Decode an instruction. */
  static DecodedInst decode(int inst) {
    FixedFields fixedFields = new FixedFields(inst);
    switch (fixedFields.opcode) {
      case 0b0110011: // R-type instructions (e.g. ADD)
        return decodeRType(fixedFields);
      case 0b0010011: // I-type instructions (e.g. ADDI)
      case 0b0000011: // also load instructions
      case 0b1100111: // also JALR
        return decodeIType(inst, fixedFields);
      case 0b0100011: // S-type (store instructions)
        return new DecodedInst(
            fixedFields,
            InstClass.S_TYPE,
            fixedFields.funct3 == 0b010 ? InstT.SW : InstT.UDEF,
            (BitExtract.bitExtractSignedInt(inst, 25, 31) << 5) | fixedFields.rd);
      case 0b0010111: // AUIPC
        return new DecodedInst(
            fixedFields,
            InstClass.U_TYPE,
            InstT.AUIPC,
            BitExtract.bitExtractInt(inst, 12, 31) << 12);
      case 0b0110111: // LUI
        return new DecodedInst(
            fixedFields, InstClass.U_TYPE, InstT.LUI, BitExtract.bitExtractInt(inst, 12, 31) << 12);
      case 0b1101111: // JAL (J-type)
        return new DecodedInst(
            fixedFields,
            InstClass.J_TYPE,
            InstT.JAL,
            (BitExtract.bitExtractInt(inst, 21, 30) << 1)
                | (BitExtract.bitExtractInt(inst, 20, 20) << 11)
                | (BitExtract.bitExtractInt(inst, 12, 19) << 12)
                | (BitExtract.bitExtractSignedInt(inst, 31, 31) << 20));
        //  B-type instrucitons
      case 0b1100011: // conditional branches
        return new DecodedInst(
            fixedFields,
            InstClass.B_TYPE,
            fixedFields.funct3 == 0b100 ? InstT.BLT : InstT.UDEF,
            BitExtract.bitExtractInt(inst, 8, 11) << 1
                | BitExtract.bitExtractInt(inst, 25, 30) << 5
                | BitExtract.bitExtractInt(inst, 7, 7) << 11
                | BitExtract.bitExtractSignedInt(inst, 31, 31) << 12);
      default:
        return new DecodedInst(fixedFields, InstClass.UNDEFINED, InstT.UDEF);
    }
  }

  private static DecodedInst decodeIType(int inst, FixedFields fixedFields) {
    int imm = BitExtract.bitExtractSignedInt(inst, 20, 31);
    switch ((fixedFields.opcode << 3) | fixedFields.funct3) {
      case (0b0010011 << 3) | 0b000:
        return new DecodedInst(fixedFields, InstClass.I_TYPE, InstT.ADDI, imm);
      case (0b0000011 << 3) | 0b010:
        return new DecodedInst(fixedFields, InstClass.I_TYPE, InstT.LW, imm);
      case (0b1100111 << 3) | 0b000:
        return new DecodedInst(fixedFields, InstClass.I_TYPE, InstT.JALR, imm);
      default:
        return new DecodedInst(fixedFields, InstClass.I_TYPE, InstT.UDEF, imm);
    }
  }

  private static DecodedInst decodeRType(FixedFields fixedFields) {
    switch ((fixedFields.funct7 << 3) | fixedFields.funct3) {
      case ((0b000000 << 3) | 0b000):
        return new DecodedInst(fixedFields, InstClass.R_TYPE, InstT.ADD);
      default:
        return new DecodedInst(fixedFields, InstClass.R_TYPE, InstT.UDEF);
    }
  }
}
