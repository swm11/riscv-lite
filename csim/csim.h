#ifndef _CSIM_H
#define _CSIM_H

#include <stdint.h>

typedef enum {
  zero=0, // hardwired to zero
  ra=1,   // return address
  sp=2,   // stack pointer
  gp=3,   // global pointer
  tp=4,   // thread pointer
  t0=5,   // temporary/alternate link register
  t1=6,   // temporary
  t2=7,   // temporary
  fp=8,   // frame pointer/saved register 0
  s1=9,   // saved register 1
  a0=10,  // function argument 0/return value 0
  a1=11,  // function argument 1/return value 1
  a2=12,  // function argument 2
  a3=13,  // function argument 3
  a4=14,  // function argument 4
  a5=15,  // function argument 5
  a6=16,  // function argument 6
  a7=17,  // function argument 7
  s2=18,  // saved register 2
  s3=19,  // saved register 3
  s4=20,  // saved register 4
  s5=21,  // saved register 5
  s6=22,  // saved register 6
  s7=23,  // saved register 7
  s8=24,  // saved register 8
  s9=25,  // saved register 9
  s10=26, // saved register 10
  s11=27, // saved register 11
  t3=28,  // temporary register 3
  t4=29,  // temporary register 4
  t5=30,  // temporary register 5
  t6=31   // temporary register 6
  } regABIname;

static const char *regABInameStr[] = {
  "zero", "ra",   "sp",   "gp",
  "tp",   "t0",   "t1",   "t2",
  "fp",   "s1",   "a0",   "a1",
  "a2",   "a3",   "a4",   "a5",
  "a6",   "a7",   "s2",   "s3",
  "s4",   "s5",   "s6",   "s7",
  "s8",   "s9",   "s10",  "s11",
  "t3",   "t4",   "t5",   "t6" };

typedef enum {Rtype=0,Itype=1,Stype=2,Btype=3,Utype=4,Jtype=5,Udef=6} instClass;

static const char *instClassStr[] = {"Rtype","Itype","Stype","Btype","Utype","Jtype","Udef"};

typedef enum {UDEF, ADD, ADDI, AUIPC, BLT, J, JAL, JALR, LW, MV, SW} inst_t;

static const char *instStr[] = {"UDEF", "ADD", "ADDI", "AUIPC", "BLTU", "J", "JAL", "JALR", "LW", "MV", "SW"};


typedef uint8_t   reg_t;
typedef uint8_t   funct_t;
typedef uint32_t  binInst_t;

typedef struct {
  instClass typ;
  inst_t inst;
  int32_t imm;
  reg_t   rd, rs1, rs2;
  funct_t opcode, funct3, funct7;
} decodedInst_t;


#endif
