package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public class LzmaAlone
{
	static public class CommandLine
	{
		public static final int kEncode = 0;
		public static final int kDecode = 1;
		public static final int kBenchmak = 2;
		
		public int Command = -1;
		public int NumBenchmarkPasses = 10;
		
		public int dictionarySize = 1 << 23;
		public boolean dictionarySizeIsDefined = false;
		
		public int Lc = 3;
		public int Lp = 0;
		public int Pb = 2;
		
		public int fb = 128;
		public boolean fbIsDefined = false;
		
		public boolean Eos = false;
		
		public int algorithm = 2;
		public int matchFinder = 1;
		
		public String inFile;
		public String outFile;
		
		boolean ParseSwitch(String s) {
			if (s.startsWith("d")) {
				dictionarySize = 1 << Integer.parseInt(s.substring(1));
				dictionarySizeIsDefined = true;
			} else if (s.startsWith("fb")) {
				fb = Integer.parseInt(s.substring(2));
				fbIsDefined = true;
			} else if (s.startsWith("a")) {
				algorithm = Integer.parseInt(s.substring(1));
			} else if (s.startsWith("lc")) {
				Lc = Integer.parseInt(s.substring(2));
			} else if (s.startsWith("lp")) {
				Lp = Integer.parseInt(s.substring(2));
			} else if (s.startsWith("pb")) {
				Pb = Integer.parseInt(s.substring(2));
			} else if (s.startsWith("eos")) {
				Eos = true;
			} else if (s.startsWith("mf")) {
				String mfs = s.substring(2);
				switch (mfs) {
					case "bt2":
						matchFinder = 0;
						break;
					case "bt4":
						matchFinder = 1;
						break;
					case "bt4b":
						matchFinder = 2;
						break;
					default:
						return false;
				}
			} else {
				return false;
			}
			return true;
		}
		
		public boolean Parse(String[] args) throws Exception {
			int pos = 0;
			boolean switchMode = true;
			for (String s : args) {
				if (s.length() == 0)
					return false;
				if (switchMode) {
					if (s.compareTo("--") == 0) {
						switchMode = false;
						continue;
					}
					if (s.charAt(0) == '-') {
						String sw = s.substring(1).toLowerCase();
						if (sw.length() == 0)
							return false;
						try {
							if (!ParseSwitch(sw))
								return false;
						} catch (NumberFormatException e) {
							return false;
						}
						continue;
					}
				}
				if (pos == 0) {
					if (s.equalsIgnoreCase("e"))
						Command = kEncode;
					else if (s.equalsIgnoreCase("d"))
						Command = kDecode;
					else if (s.equalsIgnoreCase("b"))
						Command = kBenchmak;
					else
						return false;
				} else if (pos == 1) {
					if (Command == kBenchmak) {
						try {
							NumBenchmarkPasses = Integer.parseInt(s);
							if (NumBenchmarkPasses < 1)
								return false;
						} catch (NumberFormatException e) {
							return false;
						}
					} else
						inFile = s;
				} else if (pos == 2)
					outFile = s;
				else
					return false;
				pos++;
				continue;
			}
			return true;
		}
	}
	
	
	static void PrintHelp()
	{
		System.out.println(
				"\nUsage:  LZMA <e|d> [<switches>...] inputFile outputFile\n" +
				"  e: encode file\n" +
				"  d: decode file\n" +
				"  b: Benchmark\n" +
				"<Switches>\n" +
				// "  -a{N}:  set compression mode - [0, 1], default: 1 (max)\n" +
				"  -d{N}:  set dictionary - [0,28], default: 23 (8MB)\n" +
				"  -fb{N}: set number of fast bytes - [5, 273], default: 128\n" +
				"  -lc{N}: set number of literal context bits - [0, 8], default: 3\n" +
				"  -lp{N}: set number of literal pos bits - [0, 4], default: 0\n" +
				"  -pb{N}: set number of pos bits - [0, 4], default: 2\n" +
				"  -mf{MF_ID}: set Match Finder: [bt2, bt4], default: bt4\n" +
				"  -eos:   write End Of Stream marker\n"
				);
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("\nLZMA (Java) 4.42 Copyright (c) 1999-2006 Igor Pavlov  2006-05-15\n");
		
		if (args.length < 1)
		{
			PrintHelp();
			return;
		}
		
		CommandLine params = new CommandLine();
		if (!params.Parse(args))
		{
			System.out.println("\nIncorrect command");
			return;
		}
		
		if (params.Command == CommandLine.kBenchmak)
		{
			int dictionary = (1 << 21);
			if (params.dictionarySizeIsDefined)
				dictionary = params.dictionarySize;
			if (params.matchFinder > 1)
				throw new Exception("Unsupported match finder");
			com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.LzmaBench.LzmaBenchmark(params.NumBenchmarkPasses, dictionary);
		}
		else if (params.Command == CommandLine.kEncode || params.Command == CommandLine.kDecode)
		{
			java.io.File inFile = new java.io.File(params.inFile);
			java.io.File outFile = new java.io.File(params.outFile);
			
			java.io.BufferedInputStream inStream  = new java.io.BufferedInputStream(new java.io.FileInputStream(inFile));
			java.io.BufferedOutputStream outStream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(outFile));
			
			boolean eos = false;
			if (params.Eos)
				eos = true;
			if (params.Command == CommandLine.kEncode)
			{
				com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.LZMA.Encoder encoder = new com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.LZMA.Encoder();
				if (!encoder.SetAlgorithm(params.algorithm))
					throw new Exception("Incorrect compression mode");
				if (!encoder.SetDictionarySize(params.dictionarySize))
					throw new Exception("Incorrect dictionary size");
				if (!encoder.SeNumFastBytes(params.fb))
					throw new Exception("Incorrect -fb value");
				if (!encoder.SetMatchFinder(params.matchFinder))
					throw new Exception("Incorrect -mf value");
				if (!encoder.SetLcLpPb(params.Lc, params.Lp, params.Pb))
					throw new Exception("Incorrect -lc or -lp or -pb value");
				encoder.SetEndMarkerMode(eos);
				encoder.WriteCoderProperties(outStream);
				long fileSize;
				if (eos)
					fileSize = -1;
				else
					fileSize = inFile.length();
				for (int i = 0; i < 8; i++)
					outStream.write((int)(fileSize >>> (8 * i)) & 0xFF);
				encoder.Code(inStream, outStream, -1, -1, null);
			}
			else
			{
				int propertiesSize = 5;
				byte[] properties = new byte[propertiesSize];
				if (inStream.read(properties, 0, propertiesSize) != propertiesSize)
					throw new Exception("input .lzma file is too short");
				com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.LZMA.Decoder decoder = new com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.LZMA.Decoder();
				if (!decoder.SetDecoderProperties2(properties))
					throw new Exception("Incorrect stream properties");
				long outSize = 0;
				for (int i = 0; i < 8; i++)
				{
					int v = inStream.read();
					if (v < 0)
						throw new Exception("Can't read stream size");
					outSize |= ((long)v) << (8 * i);
				}
				if (decoder.Code(inStream, outStream, outSize,null) != HRESULT.S_OK)
					throw new Exception("Error in data stream");
			}
			outStream.flush();
			outStream.close();
			inStream.close();
		}
		else
			throw new Exception("Incorrect command");
	}
}
