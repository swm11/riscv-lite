import java.lang.Math;
import java.io.IOException;

class processor
{
    public enum executeState {STOPPED, RUNNING};
    
    private final String regABInameStr[] = {
      "zero", "ra",   "sp",   "gp",
      "tp",   "t0",   "t1",   "t2",
      "fp",   "s1",   "a0",   "a1",
      "a2",   "a3",   "a4",   "a5",
      "a6",   "a7",   "s2",   "s3",
      "s4",   "s5",   "s6",   "s7",
      "s8",   "s9",   "s10",  "s11",
      "t3",   "t4",   "t5",   "t6" };

    private enum instClass {Rtype, Itype, Stype, Btype, Utype, Jtype, Udef};
    
    private enum inst_t {UDEF, ADD, ADDI, AUIPC, BLT, J, JAL, JALR, LW, LUI, MV, SW};

    // Decoded instruction type
    private class decodedInst {
	public instClass typ;  // decoded instruction type
	public inst_t    inst; // decoded instruction
	public int       imm;  // decoded immediate operand
	public byte      rd, rs1, rs2; // registers
	public byte      opcode, funct3, funct7;
    }

    // Architectural state of the processor
    private class archState {
	int pc, nextpc;  // current and next program counter values
	int rf[];        // register file
    }

    // Storage for the memory and architectural state
    private memory mem;
    private archState archst;

    /**********************************************************************
     * Initialise memory and architectural state
     **********************************************************************/
    public void processor(
			  int memsizebytes,
			  String programfilepath,
			  int startPC
			  ) throws IOException
    {
	this.mem = new memory();
	this.mem.memory(memsizebytes, programfilepath);
	archst = new archState();
	archst.nextpc = startPC;
	archst.pc = -1; // value should never be looked at, so set to an invalid value
	archst.rf = new int[32];
	for(int r=0; r<32; r++) {
	    archst.rf[r] = 0;
	}
    }
    
    /**********************************************************************
     * Decode an instruction
     **********************************************************************/
    private decodedInst decode(int inst)
    {
	decodedInst d = new decodedInst();
	d.opcode = bitextract.bitExtractByte(inst,0,6);
	d.rd = bitextract.bitExtractByte(inst,7,11);
	d.rs1 = bitextract.bitExtractByte(inst,15,19);
	d.rs2 = bitextract.bitExtractByte(inst,20,24);
	d.funct3 = bitextract.bitExtractByte(inst,12,14);
	d.funct7 = bitextract.bitExtractByte(inst,25,31);
	switch(d.opcode) {
	case 0b0110011: // R-type instructions (e.g. ADD)
	    d.typ = instClass.Rtype;
	    d.imm = 0;
	    switch((d.funct7<<3) | d.funct3) {
	    case ((0b000000<<3) | 0b000): d.inst = inst_t.ADD; break;
	    default: d.inst = inst_t.UDEF;
	    }
	    break;
                  
	case 0b0010011: // I-type instructions (e.g. ADDI)
	case 0b0000011: // also load instructions
	case 0b1100111: // also JALR
	    d.typ = instClass.Itype;
	    d.imm = bitextract.bitExtractSignedInt(inst,20,31);
	    switch((d.opcode<<3) | d.funct3) {
	    case (0b0010011<<3) | 0b000: d.inst = inst_t.ADDI; break;
	    case (0b0000011<<3) | 0b010: d.inst = inst_t.LW;   break;
	    case (0b1100111<<3) | 0b000: d.inst = inst_t.JALR; break;
	    default: d.inst = inst_t.UDEF;
	    }
	    break;
	case 0b0100011: // S-type (store instructions)
	    d.typ = instClass.Stype;
	    d.imm = (bitextract.bitExtractSignedInt(inst,25,31)<<5) | d.rd;
	    switch(d.funct3) {
	    case 0b010: d.inst = inst_t.SW; break;
	    default: d.inst = inst_t.UDEF;
	    }
	    break;
	case 0b0010111: // AUIPC
	    d.typ = instClass.Utype;
	    d.imm = bitextract.bitExtractInt(inst,12,31)<<12;
	    d.inst = inst_t.AUIPC;
	    break;
	case 0b0110111: // LUI
	    d.typ = instClass.Utype;
	    d.imm = bitextract.bitExtractInt(inst,12,31)<<12;
	    d.inst = inst_t.LUI;
	    break;
	case 0b1101111: // JAL (J-type)
	    d.typ = instClass.Jtype;
	    d.imm =
		(bitextract.bitExtractInt(inst,21,30)<< 1) |
		(bitextract.bitExtractInt(inst,20,20)<<11) |
		(bitextract.bitExtractInt(inst,12,19)<<12) |
		(bitextract.bitExtractSignedInt(inst,31,31)<<20);
	    d.inst = inst_t.JAL;
	    break;
	                //  B-type instrucitons
	case 0b1100011: // conditional branches
	    d.typ = instClass.Btype;
	    d.imm = bitextract.bitExtractInt(inst,8,11)<<1 |
		bitextract.bitExtractInt(inst,25,30)<<5 |
		bitextract.bitExtractInt(inst,7,7)<<11 |
		bitextract.bitExtractSignedInt(inst,31,31)<<12;
	    switch(d.funct3) {
	    case 0b100: d.inst = inst_t.BLT; break;
	    default: d.inst = inst_t.UDEF;
	    }
	    break;
	    
	default:
	    d.typ = instClass.Udef;
	    d.inst = inst_t.UDEF;
	    d.imm = 0;
	}

	return d;
    }


    public executeState executeStep()
    {
	decodedInst d;
	
	archst.pc = archst.nextpc;  // move onto the next pc
	archst.rf[0] = 0; // ensure register zero is always 0
	d = decode(this.mem.load(archst.pc)); // fetch and decode instruction
	archst.nextpc = archst.pc+4;
	switch(d.inst) {
	case ADD:
	    archst.rf[d.rd] = archst.rf[d.rs1] + archst.rf[d.rs2];
	    break;
	case ADDI:
	    archst.rf[d.rd] = archst.rf[d.rs1] + d.imm;
	    break;
	case AUIPC:
	    archst.rf[d.rd] = archst.pc + d.imm;
	    break;
	case LUI:
	    archst.rf[d.rd] = d.imm;
	    break;
	case BLT:
	    if(archst.rf[d.rs1] < archst.rf[d.rs2])
		archst.nextpc = archst.pc + d.imm;
	    break;
	case JAL:
	    archst.nextpc = archst.pc + d.imm;
	    archst.rf[1] = archst.pc+4; // x1 = ra (return address)
	    break;
	case JALR:
	    archst.nextpc = archst.rf[d.rs1] + d.imm;
	    archst.rf[1] = archst.pc+4; // x1 = ra (return address)
	    break;
	case LW:
	    archst.rf[d.rd] = this.mem.load(archst.rf[d.rs1] + d.imm);
	    break;
	case SW:
	    this.mem.store(archst.rf[d.rs1] + d.imm, archst.rf[d.rs2]);
	    break;
	default:
	    System.out.format("ERROR: Undefined instruction at pc=0x%08x\n",archst.pc);
	    archst.nextpc=archst.pc; // trigger stop condition
	}

	return archst.nextpc==archst.pc ? executeState.STOPPED
	                                : executeState.RUNNING;
    }

    
    /**********************************************************************
     * Dump memory with instructions decoded
     **********************************************************************/
    public void decodedump(
			   memory mem,
			   int lowerBound,
			   int upperBound)
    {
	int a, m;
	for(a=lowerBound; a<=upperBound; a=a+4) {
	    m = this.mem.load(a);
	    decodedInst d = decode(m);
	    System.out.format(
	        "0x%04x: 0x%08x opcode=%s typ=%-5s inst=%-5s rd=%-4s rs1=%-4s rs2=%-4s imm=0x%08x=%d\n",
		a, m,
		String.format("%7s", Integer.toBinaryString(d.opcode)).replace(' ', '0'), // nasty hack to get exaclty 7 binary digits
		d.typ.name(), d.inst.name(),
		regABInameStr[d.rd],
		regABInameStr[d.rs1], regABInameStr[d.rs2],
		d.imm, d.imm);
	}
    }

    /**********************************************************************
     * Report on instruction executed
     **********************************************************************/
    public void traceExecutedInstruction()
    {
	decodedInst d;
	d = decode(this.mem.load(archst.pc)); // fetch and decode instruction
	System.out.format(
            "pc=0x%08x inst=%5s rd=x%02d=%4s=%-8d rs1=%4s=%-8d rs2=%4s=%-8d imm=0x%08x=%d\n",
	    archst.pc,
	    d.inst.name(),
	    d.rd, regABInameStr[d.rd], archst.rf[d.rd], 
	    regABInameStr[d.rs1], archst.rf[d.rs1],
	    regABInameStr[d.rs2], archst.rf[d.rs2],
	    d.imm, d.imm);
	if(archst.nextpc != archst.pc+4) {
	    System.out.format("--------------------jump: 0x%08x->0x%08x --------------------\n", archst.pc, archst.nextpc);
	}
	if(archst.nextpc==archst.pc) {
	    System.out.format("--------------------STOPPED--------------------\nRegister map:\n");
	    for(int r=0; r<32; r++) {
		System.out.format("  x%02d = %4s = 0x%08x = %d\n",
				  r, regABInameStr[r], archst.rf[r], archst.rf[r]);
	    }
	}
    }
}
