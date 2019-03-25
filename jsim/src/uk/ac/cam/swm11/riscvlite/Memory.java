package uk.ac.cam.swm11.riscvlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class Memory {
  private int[] mem;

  private Memory(int[] mem) {
    this.mem = mem;
  }

  // initialise a memory of a given size with a binary image from a file
  static Memory initialize(int memsizebytes, String filepath) throws IOException {
    // initialise the memory
    int memsizewords = memsizebytes / 4;
    int[] mem = new int[memsizewords]; // zero initialised
    System.out.format(
        "Memory size = %d words = %d bytes = %d KiB\n",
        mem.length, mem.length * 4, mem.length * 4 / 1024);
    // load binary image into the memory
    byte[] b = Files.readAllBytes(Paths.get(filepath));
    // copy bytes from byte buffer into our word-sized mem[] array
    for (int a = 0; (a < mem.length) && ((a * 4 + 3) < b.length); a++) {
      mem[a] =
          ((b[a * 4 + 3] & 0xff) << 24)
              | ((b[a * 4 + 2] & 0xff) << 16)
              | ((b[a * 4 + 1] & 0xff) << 8)
              | ((b[a * 4 + 0] & 0xff));
    }

    return new Memory(mem);
  }

  private boolean checkValidAddress(int byteaddress) {
    // check:
    boolean valid =
        ((byteaddress & 0x3) == 0) //  alignment requirement
            && ((byteaddress / 4) < this.mem.length) //  upper bound
            && (byteaddress >= 0); //  lower bound
    if (!valid) {
      if ((byteaddress & 0x3) == 0) {
        System.out.format(
            "ERROR: out of range address sent to memory: 0x%08x = %d\n", byteaddress, byteaddress);
      } else {
        System.out.format(
            "ERROR: incorrectly aligned address sent to memory: 0x%08x = 0b%s\n",
            byteaddress,
            String.format("%7s", Integer.toBinaryString(byteaddress))
                .replace(' ', '0')); // nasty hack to get exaclty 32 binary digits
      }
    }
    return valid;
  }

  int load(int byteaddress) {
    if (this.checkValidAddress(byteaddress)) return this.mem[byteaddress / 4];
    else return 0xdead0000;
  }

  void store(int byteaddress, int data) {
    if (byteaddress == 0xf0000000) { // magic output device detected
      System.out.format("**********************************************************************\n");
      System.out.format("* RESULT = 0x%08x = %d\n", data, data);
      System.out.format("**********************************************************************\n");
    } else if (this.checkValidAddress(byteaddress)) this.mem[byteaddress / 4] = data;
  }

  void hexdump(int to, int from) {
    from = from / 4;
    to = to / 4;
    if (from < 0) from = 0;
    if (to >= mem.length) to = mem.length - 1;
    for (int a = to; a <= from; a++)
      System.out.format("mem[0x%08x] = 0x%08x = %d\n", a * 4, this.mem[a], this.mem[a]);
  }
}
