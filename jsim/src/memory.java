import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

class memory
{
    private int mem[];

    private boolean checkValidAddress(int byteaddress)
    {
	return ((byteaddress & 0x3)==0)
	    && ((byteaddress/4)<this.mem.length)
	    && (byteaddress>=0);
    }
    
    public int load(int byteaddress)
    { 
        if(this.checkValidAddress(byteaddress))
	    return this.mem[byteaddress/4];
	else
	    return 0xdead0000;
    }

    public void store(int byteaddress, int data)
    { 
        if(this.checkValidAddress(byteaddress))
	    this.mem[byteaddress/4] = data;
	// TODO raise exception on invalid address?
    }

    public void hexdump(int to, int from)
    {
	from = from/4;
	to = to/4;
	if(from<0)
	    from = 0;
	if(to>=mem.length)
	    to = mem.length-1;
	for(int a=to; a<=from; a++)
	    System.out.format("mem[0x%08x] = 0x%08x = %d\n",
			      a, this.mem[a], this.mem[a]);
    }
    
    public void memory(int memsizebytes, String filepath) throws IOException
    {
	// initialise the memory
	int memsizewords = memsizebytes/4;
	this.mem = new int [memsizewords];
	for(int a=0; a<memsizewords; a++) {
	    this.mem[a] = 0;
	}
	// load binary image into the memory
        byte [] b = Files.readAllBytes(Paths.get(filepath));
	for(int a=0; (a<this.mem.length) && ((a*4+3)<b.length); a++) {
	    this.mem[a] = (b[a*4+3]<<24)
		        | (b[a*4+2]<<16)
    		        | (b[a*4+1]<< 8)
		        | (b[a*4+0]);
	}
	/*
	File file = new File(filepath);
	if(file.length() < this.memsizewords*4) {
	    DataInputStream dis = new DataInputStream(new FileInputStream(file));
	    dis.readFully(this.mem);
	    dis.close();
	} else {
	    println("Insufficient space to read file");
	}
	*/
    } 
}
