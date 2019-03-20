import java.io.IOException;

class jsim
{ 
    public static void main(String args[]) 
    { 
        memory mem=new memory();
	processor.executeState ps;
	processor proc=new processor();
	try {
	    // initialise processor and load program binary
	    proc.processor(65*1024, "../fib/build/mem.bin", 0);
	} catch (IOException e) {
	    System.out.format("ERROR: Failed to read binary initialisation file.\n");
	}
	System.out.format("Decoded dump of the initial memory:\n");
	proc.decodedump(mem,0,50*4);
	// step through execution until the stop condition is met
	do {
	    ps = proc.executeStep();
	    proc.traceExecutedInstruction();
	} while(ps==processor.executeState.RUNNING);
    } 
} 
