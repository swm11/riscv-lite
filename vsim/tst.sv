`timescale 1ns/1ns

import types::*;

module tst
  (
   output logic clk,
   output logic rst,
   output 		rvwordT pc,
   output 		EpochT pc_epoch
   );

`include "decode.sv"

   rvwordT pc;
   EpochT pc_epoch;

   rvwordT ir; // instruction wires (from a register in the memory)
   EpochT ir_epoch; // instruction register epoch

   decodedInstT d;

   rvwordT jumpPC;
   EpochT jumpEpoch;
   ExecuteStateT executeState;

   MemControlT dmem_control;
   rvwordT dmem_addr, dmem_writedata, dmem_readdata;
   logic 		dmem_readdata_valid;
   
   memory 
     #(
       .INIT_FILE("../fib/build/mem.txt"),
       .MEM_WIDTH(16)
       ) do_mem
       (
    .clk(clk),
    .rst(rst),
    .iaddr(pc>>2),
    .idataout(ir),
    .dwe(dmem_signal==MEM_WRITE),
    .daddr(dmem_addr),
    .ddatain(dmem_writedata),
    .ddataout(dmem_readdata)
    );

   fetch
     #(
	   .START_PC=0
	   ) do_if
	   (
		.clk(clk),
		.rst(rst),
		// to instruction memory
		.pc(pc),
		.pc_epoch(pc_epoch),
		// from execute
		.jumpPC(jumpPC),
		.jumpEpoch(jumpEpoch),
		.executeState(executeState)
		);

   // instruction decode:
   assign d = decode(ir);

   execute do_ex
	 (
	  // clock/reset
	  .clk(clk),
	  .rst(rst),
	  // decoded instruction
	  .d(d),
	  .d_epoch(ir_epoch),
	  // communication to fetch unit
	  .jumpPC(jumpPC),
	  .jumpEpoch(jumpEpoch),
	  .executeState(executeState),
	  // communication to data memory
	  .dmem_control(dmem_control),
	  .dmem_addr(dmem_addr),
	  .dmem_writedata(dmem_writedata),
	  .dmem_readdata(dmem_readdata),
	  .dmem_readdata_valid(dmem_readdata_valid)
	  );
   
      
   initial begin
      clk = 0;
      rst = 1;
      #20 rst = 0;
      #1000 $finish();
   end

   always #5 clk <= !clk;

   always @(posedge clk or posedge rst)
     if(rst)
       begin
		  ir_epoch <= EPOCH_INVALID;
		  dmem_readdata_valid <= 0;
       end
     else
       begin
		  // embedded memory takes one cycle, so propagate pc_epoch at that rate:
		  ir_epoch <= pc_epoch;
		  // embedded memory data read takes one cycle
		  dmem_readdata_valid <= dmem_control == MEM_READ;
       end

   always @(negedge clk)
     if(!rst)
       begin
          $display("%05t: mem[0x%08x] = 0x%08x  op=0b%07B  type=%s  inst=%s",
                   $time,
                   pc,ir,
                   d.fields.opcode,
                   d.dec.typ,
                   d.dec.inst);
          $display("%05t: funct7=%07B  rs2=%5s  rs1=%5s  funct3=%03B  rd=%5s  imm=0x%08x=%d",
                   $time,
                   d.fields.funct7,
                   d.fields.rs2,
                   d.fields.rs1,
                   d.fields.funct3,
                   d.fields.rd,
                   d.dec.imm,
                   d.dec.imm);
       end
   
endmodule // tst

       
