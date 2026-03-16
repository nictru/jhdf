/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

public class ByteShuffleFilter implements Filter {

	@Override
	public int getId() {
		return 2;
	}

	@Override
	public String getName() {
		return "shuffle";
	}

	@Override
	public byte[] decode(byte[] data, int[] filterData) {
		// When filterData is empty (no explicit cd_values in the HDF5 filter pipeline),
		// fall back to elementSize=1 which is a no-op (identity unshuffle).
		final int elementSize = filterData.length > 0 ? filterData[0] : 1;
		return decode(data, filterData, elementSize);
	}

	@Override
	public byte[] decode(byte[] data, int[] filterData, int elementSize) {
		// Use filterData[0] if present, otherwise use the provided elementSize fallback.
		final int dataSize = filterData.length > 0 ? filterData[0] : elementSize;

		// A quick shortcut if no shuffling is needed
		if (dataSize == 1) {
			return data;
		}

		final int elements = data.length / dataSize;

		// shuffle doesn't change the size of the data it rearranges it
		final byte[] out = new byte[data.length];

		int pos = 0;
		for (int i = 0; i < dataSize; i++) {
			for (int j = 0; j < elements; j++) {
				out[j * dataSize + i] = data[pos];
				pos++; // step through the input array
			}
		}

		// Very special case here for mismatched length e.g use deflate then shuffle
		// Not sure if there is actually any good use case
		if(pos < data.length) {
			// In the overrun section no shuffle is done just a straight copy
			System.arraycopy(data, pos, out, pos, data.length - pos);
		}

		return out;
	}
}
