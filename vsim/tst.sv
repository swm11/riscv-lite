`timescale 1ns/1ns

import types::*;

module tst(output logic clk,
       output logic rst,
       output logic [31:0] pc,
       output logic [31:0] next_pc);

`include "decode.sv"
   
   rvwordT ir; // instruction wires (from a register in the memory)
//   decodedInstT d = decode(ir); // decode the instruction all the time
   decodedInstT d;
   
   memory 
     #(
       .INIT_FILE("../fib/build/mem.txt"),
       .MEM_WIDTH(16)
       ) mem
       (
    .clk(clk),
    .rst(rst),
    .iaddr(next_pc>>2),
    .idataout(ir),
    .dwe(1'd0),
    .daddr(32'd0),
    .ddatain(32'd0),
    .ddataout()
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
          pc <= -1;
          next_pc <= 0;
       end
     else
       begin
          pc <= next_pc;
          next_pc <= next_pc+4;
       end

   always_comb    d = decode(ir);

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

       
