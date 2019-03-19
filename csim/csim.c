#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <assert.h>
#include "csim.h"


void
loadbin(const char *filename,
	uint32_t *mem,
	const uint32_t memsizewords)
{
  FILE *fp;
  size_t size, freadSize;

  fp = fopen(filename, "r");
  if(fp == NULL) { // error detected
    printf("ERROR: when reading file %s\n",filename);
    exit(1);
  }
  fseek(fp, 0, SEEK_END);
  size = ftell(fp)/sizeof(uint32_t);
  fseek(fp, 0, SEEK_SET);
  if(size>memsizewords)
    size=memsizewords;
  freadSize = fread(mem, sizeof(uint32_t), size, fp);
  if(freadSize != size) {
    printf("ERROR: failed to read all of %s - read %ld bytes but expected %ld bytes\n",filename, freadSize, size);
    exit(1);
  }
  fclose(fp);
}


void
hexdump(uint32_t *mem,
	uint32_t lowerBound,
	uint32_t upperBound)
{
  int j;
  for(j=lowerBound; j<=upperBound; j++)
    printf("0x%08x: 0x%08x\n", j, mem[j]);
}


uint32_t
bitExtract(
	   uint32_t bits,
	   uint8_t lower,
	   uint8_t upper
	   )
{
  assert(lower<=upper);
  return (bits>>lower) & ((1<<(upper-lower+1))-1);
}


int32_t
bitExtractSignExtend(
	   uint32_t bits,
	   uint8_t lower,
	   uint8_t upper
	   )
{
  assert(lower<=upper);
  uint32_t b = (bits>>lower) & ((1<<(upper-lower+1))-1);
  int32_t s = -((bits>>upper) & 0x1);
  return (s << (upper-lower+1)) | b;
}

void
decode(binInst_t inst, decodedInst_t *d)
{
  d->opcode = bitExtract(inst,0,6);
  d->rd = bitExtract(inst,7,11);
  d->rs1 = bitExtract(inst,15,19);
  d->rs2 = bitExtract(inst,20,24);
  d->funct3 = bitExtract(inst,12,14);
  d->funct7 = bitExtract(inst,25,31);
  switch(d->opcode) {
  case 0b0110011: // R-type instructions (e.g. ADD)
    d->typ = Rtype;
    d->imm = 0;
    switch((d->funct7<<3) | d->funct3) {
    case ((0b000000<<3) | 0b000): d->inst = ADD; break;
    default: d->inst = UDEF;
    }
    break;
                  
  case 0b0010011: // I-type instructions (e.g. ADDI)
  case 0b0000011: // also load instructions
  case 0b1100111: // also JALR
    d->typ = Itype;
    d->imm = bitExtractSignExtend(inst,20,31);
    switch((d->opcode<<3) | d->funct3) {
    case (0b0010011<<3) | 0b000: d->inst = ADDI; break;
    case (0b0000011<<3) | 0b010: d->inst = LW;   break;
    case (0b1100111<<3) | 0b000: d->inst = JALR; break;
    default: d->inst = UDEF;
    }
    break;
  case 0b0100011: // S-type (store instructions)
    d->typ = Stype;
    d->imm = (bitExtractSignExtend(inst,25,31)<<5) | d->rd;
    switch(d->funct3) {
    case 0b010: d->inst = SW; break;
    default: d->inst = UDEF;
    }
    break;
  case 0b0010111: // AUIPC
    d->typ = Utype;
    d->imm = bitExtract(inst,12,31)<<12;
    d->inst = AUIPC;
    break;
  case 0b1101111: // JAL (J-type)
    d->typ = Jtype;
    d->imm =
      (bitExtract(inst,21,30)<< 1) |
      (bitExtract(inst,20,20)<<11) |
      (bitExtract(inst,12,19)<<12) |
      (bitExtractSignExtend(inst,31,31)<<20);
    d->inst = JAL;
    break;
                  //  B-type instrucitons
  case 0b1100011: // conditional branches
    d->typ = Btype;
    d->imm = bitExtract(inst,8,11)<<1 |
             bitExtract(inst,25,30)<<5 |
             bitExtract(inst,7,7)<<11 |
             bitExtractSignExtend(inst,31,31)<<12;
    switch(d->funct3) {
    case 0b100: d->inst = BLT; break;
    default: d->inst = UDEF;
    }
    break;

  default:
    d->typ = Udef;
    d->inst = UDEF;
    d->imm = 0;
  }
}



// convert integers to binary strings
void
myitob(uint32_t val, char * buf, uint32_t nbits) {
  for(int i=nbits; i>0; --i, val = val>>1)
    buf[i-1] = "01"[val & 1];
  buf[nbits]='\0';
}


void
decodedump(uint32_t *mem,
	   uint32_t lowerBound,
	   uint32_t upperBound)
{
  int j;
  for(j=lowerBound; j<=upperBound; j++) {
    decodedInst_t d;
    decode(mem[j], &d);
    char buf[33];
    myitob(d.opcode,buf,7);
    printf("0x%04x: 0x%08x opcode=%s typ=%5s inst=%5s rd=%4s rs1=%4s rs2=%4s imm=0x%08x=%d\n",
    	   j<<2, mem[j], buf, instClassStr[d.typ], instStr[d.inst],
	   regABInameStr[d.rd], regABInameStr[d.rs1], regABInameStr[d.rs2],
	   d.imm, d.imm);
  }
}


void
processor(uint32_t *mem, int memsizewords)
{
  uint32_t pc;
  uint32_t oldpc;
  int32_t  rf[32]; // register file
  uint32_t ir; // instruction register
  uint32_t addr; // temporary address value
  int r;

  for(r=0; r<32; r++)
    rf[r] = 0;
  
  for(oldpc=1,pc=0; (oldpc!=pc); ) {
    rf[0] = 0;
    oldpc = pc;
    ir = mem[pc>>2];
    pc = pc+4;
    decodedInst_t d;
    decode(ir, &d);
    switch(d.inst) {
    case ADD:
      rf[d.rd] = rf[d.rs1] + rf[d.rs2];
      break;
    case ADDI:
      rf[d.rd] = rf[d.rs1] + d.imm;
      break;
    case AUIPC:
      rf[d.rd] = oldpc + d.imm; // x2 = sp
      break;
    case BLT:
      if(rf[d.rs1] < rf[d.rs2])
	pc = oldpc + d.imm;
      break;
    case JAL:
      pc = oldpc + d.imm;
      rf[1] = oldpc+4; // x1 = ra (return address)
      break;
    case JALR:
      pc = rf[d.rs1] + d.imm;
      rf[1] = oldpc+4; // x1 = ra (return address)
      break;
    case LW:
      addr = (rf[d.rs1] + d.imm)/4; // shift/div does byte-to-word addressing fix (TODO: clean up?)
      if((addr>=0) && (addr<memsizewords))
	rf[d.rd] = mem[addr];
      else
	printf("ERROR: address %d is out of range\n",addr);
      break;
    case SW:
      addr = (rf[d.rs1] + d.imm)/4;
      if((addr>=0) && (addr<memsizewords))
	mem[addr] = rf[d.rs2];
      else
	printf("ERROR: address %d is out of range\n",addr);
      break;
    default:
      printf("ERROR: Undefined instruction at pc=0x%08x\n",oldpc);
      pc=oldpc; // trigger stop condition
    }
    printf("pc=0x%08x inst=%5s rd=x%02d=%4s=%-8d rs1=%4s=%-8d rs2=%4s=%-8d imm=0x%08x=%d\n",
	   oldpc,
	   instStr[d.inst],
	   d.rd, regABInameStr[d.rd], rf[d.rd], 
	   regABInameStr[d.rs1], rf[d.rs1],
	   regABInameStr[d.rs2], rf[d.rs2],
	   d.imm, d.imm);	   
  }
  printf("STOPPED\nRegister map:\n");
  for(r=0; r<32; r++) {
    printf("  x%02d = %4s = 0x%08x = %d\n",
	   r, regABInameStr[r], rf[r], rf[r]);
  }
}

int
main(void)
{
  uint32_t memsizewords = 64*1024/4;  // 64kB = 8k words of memory (stack grows down from 64kB point)
  uint32_t *mem = (uint32_t *) malloc(memsizewords); 
  loadbin("../fib/build/mem.bin", mem, memsizewords);
  //hexdump(mem,0,40);
  decodedump(mem,0,40);
  processor(mem,memsizewords);
  return 0;
}
