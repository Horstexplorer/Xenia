package de.netbeacon.d43z.one.algo;

import java.util.Arrays;

public class LiamusJaccard{

	public static float similarityCoefficient(String a, String b, int nGramSize){
		if(nGramSize < 1){
			throw new IllegalArgumentException("Illegal n-gram size");
		}
		BitArray64 stringABits = hashString(a, nGramSize);
		BitArray64 stringBBits = hashString(b, nGramSize);
		return stringABits.popCntJaccard(stringBBits);
	}

	public static float similarityCoefficient(BitArray64 a, BitArray64 b){
		return a.popCntJaccard(b);
	}

	public static BitArray64 hashString(String s, int nGramSize){
		if(s == null){
			return null;
		}
		final int JACCARD_ARRAY_32WORDS = 16;
		final int JACCARD_HASHBITS = (JACCARD_ARRAY_32WORDS * 64) - 1;
		BitArray64 stringBits = new BitArray64(JACCARD_ARRAY_32WORDS);
		for(int i = 0; i < s.length() - nGramSize + 1; i++){
			int hash = 0;
			for(int h = 0; h < nGramSize; h++){
				hash = 31 * hash + s.charAt(i + h);
			}
			hash &= JACCARD_HASHBITS;
			stringBits.setBit(hash);
		}
		return stringBits;
	}

	public static class BitArray64{

		private final long[] words;

		public BitArray64(int bits64){
			this.words = new long[bits64];
		}

		public BitArray64(BitArray64 other){
			this.words = new long[other.words.length];
			System.arraycopy(other.words, 0, words, 0, words.length);
		}

		public void clear(){
			Arrays.fill(words, 0);
		}

		public void setBit(int bitIdx){
			int wordIdx = bitIdx >>> 6;
			int bitShift = bitIdx & 0x3F;
			words[wordIdx] |= (1L << bitShift);
		}

		public int popCntUnion(BitArray64 other){
			int bits = 0;
			for(int i = 0; i < words.length; i++){
				bits += Long.bitCount(words[i] | other.words[i]);
			}
			return bits;
		}

		public int popCntIntersect(BitArray64 other){
			int bits = 0;
			for(int i = 0; i < words.length; i++){
				bits += Long.bitCount(words[i] & other.words[i]);
			}
			return bits;
		}

		public float popCntJaccard(BitArray64 other){
			int bitsU = 0;
			int bitsI = 0;
			for(int i = 0; i < words.length; i++){
				long wordA = words[i];
				long wordB = other.words[i];
				bitsU += Long.bitCount(wordA | wordB);
				bitsI += Long.bitCount(wordA & wordB);
			}
			return (float) bitsI / (float) bitsU;
		}

		public String toString(){
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < words.length; i++){
				String binstr = Long.toBinaryString(words[i]);
				binstr = "0".repeat(64 - binstr.length()) + binstr;
				sb.append(binstr);
			}
			return sb.toString();
		}

	}

}
