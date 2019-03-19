import java.io.IOException;
//import memory;

class jsim
{ 
    public static void main(String args[]) 
    { 
        memory mem=new memory();
	try {
	    mem.memory(64*1024, "../fib/build/mem.bin");
	} catch (IOException e) {
	    System.out.format("ERROR: Failed to read binary initialisation file.\n");
	}
	System.out.format("Hex dump:\n");
	mem.hexdump(0,50*4);
    } 
} 
