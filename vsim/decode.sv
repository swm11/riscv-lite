`include "types.svh"

function automatic decodedInstT decode(rvwordT binaryInstruction);
   // assign binary representation to packed struct to extract fixed fields
   assert($bits(decode.fields)==$bits(rvwordT));
   decode.fields = binaryInstruction;
   unique case(decode.fields.opcode)
     7'b0110011 : decode.dec = decodeRType(decode.fields);
     default:     decode.dec = '{typ: UNDEFINED, inst: UDEF, imm: 0};
   endcase // case {d.inst.opcode)
endfunction // decode


function automatic decodeT decodeRType(InstFixedFieldsT fields);
   unique case({fields.funct7, fields.funct3})
     {7'b0000000, 3'b000}: return '{typ: R_TYPE, inst: ADD,  imm: 0};
     default:              return '{typ: UNDEFINED, inst: UDEF, imm: 0};
   endcase
endfunction // decodeRType

