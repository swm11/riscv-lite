import types::*;

module execute
  (
   // clock/reset
   input 	   clk,
   input 	   rst,
   // decoded instruction in
   input 	   decodedInstT d,
   input 	   EpochT d_epoch,
   // orders to instruction fetch
   output 	   rvwordT jumpPC,
   output 	   EpochT jumpEpoch,
   output 	   ExecuteStateT executeState,
   // data memory interface
   output 	   MemControlT dmem_control,
   output 	   rvwordT dmem_addr,
   output 	   rvwordT dmem_writedata,
   input 	   rvwordT dmem_readdata,
   input logic dmem_readdata_valid
   );

   // registers
   rvwordT rf[0:31];  // register file
   EpochT epoch;
   
   // wires
   rvwordT rf_rs1, rf_rs2;
   rvwordT writeBackValue;
   regT    writeBackReg;
   
   always_ff @(posedge clk or posedge rst)
	 if(rst)
	   begin
		  rf[0] <= 0;
		  epoch <= EPOCH_RED;
	   end
	 else
	   if(d_epoch == epoch)
		 begin
			if(jumpEpoch != EPOCH_INVALID)
			  epoch <= jumpEpoch;
			rf[writeBackReg] <= writeBackValue;
		 end
	   else
		 $display("%05t: execute NOP since d_epoch=%s but epoch=%s",
				  $time, d_epoch, epoch);
   
   always_comb
	 begin
		writeBackReg = X_zero;
		writeBackValue = 0;
		rf_rs1 = rf[d.fields.rs1];
		rf_rs2 = rf[d.fields.rs2];
		jumpEpoch = EPOCH_INVALID;
		executeState = EX_RUNNING;
		case(d.dec.inst)
		  ADD:
			begin
			   writeBackValue = rf_rs1 + rf_rs2;
			   writeBackReg = d.fields.rd;
			end
		  ADDI:
			begin
			   writeBackValue = rf_rs1 + d.dec.imm;
			   writeBackReg = d.fields.rd;
			end
		  AUIPC:
			begin
			   writeBackValue = d.pc + d.dec.imm;
			   writeBackReg = d.fields.rd;
			end
		  LUI: 
			begin
			   writeBackValue = d.dec.imm;
			   writeBackReg = d.fields.rd;
			end
		  BLT:
			if(rf_rs1 < rf_rs2)
			  begin
				 jumpPC = d.pc + d.dec.imm;
				 jumpEpoch = nextEpochColour(epoch);
			  end
		  JAL:
			begin
			   jumpPC = d.pc + d.dec.imm;
			   jumpEpoch = nextEpochColour(epoch);
			   writeBackValue = d.pc + 4;
			   writeBackReg = X_ra;
			end
		  JALR:
			begin
			   jumpPC = rf_rs1 + d.dec.imm;
			   jumpEpoch = nextEpochColour(epoch);
			   writeBackValue = d.pc + 4;
			   writeBackReg = X_ra;
			end
		  // TODO: LW and SW
		  default:
			$display("ERROR: Undefined instruction at pc=0x%08x",d.pc);
		endcase
	 end // always_comb
   
   
endmodule // execute

   
			
