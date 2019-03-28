`include "types.svh"

module memory
  #(
    parameter INIT_FILE = "mem.hex",
    parameter MEM_WIDTH = 16
  )(// clock/reset interface
   input logic clk,
   input logic rst,
   // instruction memory interface
   input  rvwordT iaddr,
   output rvwordT idataout,
   // data memory interface
   input logic    dwe, // write enable (otherwise read)
   input rvwordT  daddr,
   input rvwordT  ddatain,
   output rvwordT ddataout
   );

   rvwordT mem[1<<MEM_WIDTH];
   initial $readmemh(INIT_FILE,mem);

   always_ff @(posedge clk)
     begin
	assert(iaddr < (1<<MEM_WIDTH));
	assert(daddr < (1<<MEM_WIDTH));
	
	idataout <= mem[iaddr];
	ddataout <= mem[daddr];
	if(dwe)
	  mem[daddr] <= ddatain;
     end

endmodule // memory

   
