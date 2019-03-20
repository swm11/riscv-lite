/******************************************************************************
 * Helper functions to extract bits from an integer
 ******************************************************************************/
class bitextract
{
    public static int bitExtractInt(
           int bits,
           int lower,
           int upper
           )
    {
	//  assert(lower<=upper);
	return (bits>>lower) & ((1<<(upper-lower+1))-1);
    }

    public static int bitExtractSignedInt(
           int bits,
           int lower,
           int upper
           )
    {
	return ((-((bits>>upper) & 0x1)) << (upper-lower+1))
	     | bitExtractInt(bits,lower,upper);
    }

    public static byte bitExtractByte(
           int bits,
           int lower,
           int upper
           )
    {
	return (byte) bitExtractInt(bits,lower,upper);
    }
}
