import types::*;

module execute
  (
   // clock/reset
   input 	   clk,
   input 	   rst,
   // decoded instruction in
   input 	   decodedInstT d,
   input 	   EpochT d_epoch
   // orders to instruction fetch
   output 	   rvwordT jumpPC,
   output 	   EpochT jumpEpoch,
   output 	   ExecuteStateT executeState
   // data memory interface
   output 	   MemControlT dmem_control,
   output 	   rvwordT dmem_addr,
   output 	   rvwordT dmem_writedata,
   input 	   rvwordT dmem_readdata,
   input logic dmem_readdata_valid
   );

   // registers
   rvwordT rf[0:31];  // register file

   // wires
   rvwordT nextpc;
   rvwordT rf_rs1, rf_rs2;
   rvwordT writeBackValue;
   regT    writeBackReg;
   
   always_ff @(posedge clk or posedge rst)
	 if(rst)
	   begin
		  pc <= startPc;
		  rf[0] <= 0;
	   end
	 else
	   begin
		  pc <= nextpc;
		  rf[writeBackReg] <= writeBackValue;
	   end

   always_comb
	 begin
		nextpc = pc+4; // default next PC  // TODO: Fix since now forward requests to fetch
		writeBackReg = X_zero;
		writeBackValue = 0;
		rf_rs1 = rf[d.rs1];
		rf_rs2 = rf[d.rs2];

		case(d.inst)
		  ADD:
			begin
			   writeBackValue = rf_rs1 + rf_rs2;
			   writeBackReg = d.rd;
			end
		  ADDI:
			begin
			   writeBackValue = rf_rs1 + d.imm;
			   writeBackReg = d.rd;
			end
		  AUIPC:
			begin
			   writeBackValue = pc + d.imm;
			   writeBackReg = d.rd;
			end
		  LUI: 
			begin
			   writeBackValue = d.imm;
			   writeBackReg = d.rd;
			end
		  BLT:
			if(rf_rs1 < rf_rs2) nextpc <= pc + d.imm;
		  JAL:
			begin
			   nextpc <= pc + d.imm;
			   writeBackValue = pc + 4;
			   writeBackReg = X_ra;
			end
		  JALR:
			begin
			   nextpc <= rf_rs1 + d.imm;
			   writeBackValue = pc + 4;
			   writeBackReg = X_ra;
			end
		  // TODO: LW and SW
		  default:
			$display("ERROR: Undefined instruction at pc=0x%08x",pc);
		endcase
	 end // always_comb
   
   
endmodule // execute

   
			
