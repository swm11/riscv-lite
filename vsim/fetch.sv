import types::*;

module fetch#(parameter START_PC=0)
   (
	// clock/reset
	input  clk,
	input  rst,
	// program counter value sent to memory
	output rvwordT pc,
	output EpochT pc_epoch,
	
	// from execute
	input  rvwordT jumpPC,
	input  EpochT jumpEpoch,
	input  ExecuteStateT executeState
	);

   EpochT if_epoch;
   assign pc_epoch = (executeState == EX_RUNNING) ? if_epoch : EPOCH_INVALID;

   always_ff @(posedge clk or posedge rst)
	 if(rst)
	   begin
		  pc <= START_PC;
		  if_epoch <= EPOCH_RED;
	   end
	 else
	   if(jumpEpoch != EPOCH_INVALID)
		 begin
			if_epoch <= jumpEpoch;
			pc <= jumpPC;
		 end
	   else if(executeState == EX_RUNNING)
		 pc <= pc+4;

endmodule // fetch
