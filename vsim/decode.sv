`include "types.svh"

//TODO: combine decode into one big table?
//TODO: do muxing of immediates elsewhere?  Encode the mux input in the decode only?

function automatic decodedInstT decode(rvwordT instr);
   // immediate extraction and sign extension
   rvwordT imm_i_type  = { {20 {instr[31]}}, instr[31:20] };
   rvwordT imm_iz_type = {            20'b0, instr[31:20] };
   rvwordT imm_s_type  = { {20 {instr[31]}}, instr[31:25], instr[11:7] };
   rvwordT imm_sb_type = { {19 {instr[31]}}, instr[31], instr[7], instr[30:25], instr[11:8], 1'b0 };
   rvwordT imm_u_type  = { instr[31:12], 12'b0 };
   rvwordT imm_uj_type = { {12 {instr[31]}}, instr[19:12], instr[20], instr[30:21], 1'b0 };
   
   // assign binary representation to packed struct to extract fixed fields
   assert($bits(decode.fields)==$bits(rvwordT));
   decode.fields = instr;

   unique case(decode.fields.opcode)
     7'b0110011:
       decode.dec = decodeRType(decode.fields);
     7'b0010011, // I-type instructions (e.g. ADDI)
     7'b0000011, // also load instructions
     7'b1100111: // also JALR
       decode.dec = decodeIType(decode.fields);
     7'b0100011: // S-type (store instructions)
       decode.dec = '{typ: S_TYPE,
		      inst: decode.fields.funct3 == 3'b010 ? SW : UDEF,
		      imm: imm_s_type};
     7'b0010111: // AUIPC
       decode.dec = '{typ: U_TYPE,
		      inst: AUIPC,
		      imm: imm_u_type};
     7'b0110111: // LUI
       decode.dec = '{typ: U_TYPE,
		      inst: LUI,
		      imm: imm_u_type};
     7'b1101111: // JAL (J-type)
       decode.dec = '{typ: J_TYPE,
		      inst: JAL,
		      imm: imm_uj_type};
      7'b1100011: // B-type instrucitons: conditional branches
       decode.dec = '{typ: B_TYPE,
		      inst: decode.fields.funct3 == 3'b100 ? BLT: UDEF,
		      imm: imm_sb_type};
     default:
       decode.dec = '{typ: UNDEFINED, inst: UDEF, imm: 32'bx};
   endcase // case {d.inst.opcode)
endfunction // decode


function automatic decodeT decodeRType(InstFixedFieldsT fields);
   unique case({fields.funct7, fields.funct3})
     {7'b0000000, 3'b000}: return '{typ: R_TYPE, inst: ADD,  imm: 32'bx};
     default:              return '{typ: UNDEFINED, inst: UDEF, imm: 32'bx};
   endcase
endfunction // decodeRType


function automatic decodeT decodeIType(InstFixedFieldsT fields);
   case({fields.funct3, fields.opcode})
     {3'b000, 7'b0010011} : return '{typ: I_TYPE,    inst: ADDI, imm: fields[31:20]};
     {3'b010, 7'b0000011} : return '{typ: I_TYPE,    inst: LW,   imm: fields[31:20]};
     {3'b000, 7'b1100111} : return '{typ: I_TYPE,    inst: JALR, imm: fields[31:20]};
     default:               return '{typ: UNDEFINED, inst: UDEF, imm: 32'bx};
   endcase
endfunction // decodeJType

   
