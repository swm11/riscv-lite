import java.io.IOException;

class jsim
{ 
    public static void main(String args[]) 
    { 
        memory mem=new memory();
	processor proc=new processor();
	try {
	    mem.memory(65*1024, "../fib/build/mem.bin");
	} catch (IOException e) {
	    System.out.format("ERROR: Failed to read binary initialisation file.\n");
	}
	// System.out.format("Hex dump:\n");
	// mem.hexdump(0,50*4);
	System.out.format("Decode dump:\n");
	proc.decodedump(mem,0,50*4);
	proc.execute(mem,0);
    } 
} 
