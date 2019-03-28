typedef logic [31:0] rvwordT;
typedef logic [6:0]  opcodeT;
typedef logic [6:0]  funct7T;
typedef logic [2:0]  funct3T;

typedef enum bit [4:0]
  {
    X_zero, X_ra,   X_sp,   X_gp,
    X_tp,   X_t0,   X_t1,   X_t2,
    X_fps0, X_s1,   X_a0,   X_a1,
    X_a2,   X_a3,   X_a4,   X_a5,
    X_a6,   X_a7,   X_s2,   X_s3,
    X_s4,   X_s5,   X_s6,   X_s7,
    X_s8,   X_s9,   X_s10,  X_s11,
    X_t3,   X_t4,   X_t5,   X_t6
  } regT;

typedef enum
  {
    R_TYPE,
    I_TYPE,
    S_TYPE,
    B_TYPE,
    U_TYPE,
    J_TYPE,
    UNDEFINED
   } InstClassT;

typedef enum
  {
    UDEF,
    ADD,
    ADDI,
    AUIPC,
    BLT,
    J,
    JAL,
    JALR,
    LW,
    LUI,
    MV,
    SW
   } InstT;

typedef struct packed
  {
     funct7T    funct7;
     regT       rs2;
     regT       rs1;
     funct3T    funct3;
     regT       rd;
     opcodeT    opcode;
   } InstFixedFieldsT;

typedef struct packed
  {
     InstClassT typ;
     InstT      inst;
     rvwordT    imm;
  } decodeT;
		     
typedef struct packed
  {
     InstFixedFieldsT fields;
     decodeT dec;
  } decodedInstT;
