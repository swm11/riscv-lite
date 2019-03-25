package uk.ac.cam.swm11.riscvlite;

/** Helper functions to extract bits from an integer. */
class BitExtract {
  static int bitExtractInt(int bits, int lower, int upper) {
    //  assert(lower<=upper);
    return (bits >> lower) & ((1 << (upper - lower + 1)) - 1);
  }

  static int bitExtractSignedInt(int bits, int lower, int upper) {
    return ((-((bits >> upper) & 0x1)) << (upper - lower + 1)) | bitExtractInt(bits, lower, upper);
  }

  static byte bitExtractByte(int bits, int lower, int upper) {
    return (byte) bitExtractInt(bits, lower, upper);
  }
}
