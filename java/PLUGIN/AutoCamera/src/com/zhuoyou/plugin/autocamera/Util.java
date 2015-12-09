package com.zhuoyou.plugin.autocamera;

import android.os.StatFs;

public class Util {
	@SuppressWarnings("deprecation")
	public static long getAvailableStore(String filePath) {
		// get sdcard path
		StatFs statFs = new StatFs(filePath);
		// get block SIZE
		long blockSize = statFs.getBlockSize();
		// getBLOCK numbers
		// long totalBlocks = statFs.getBlockCount();
		// get available Blocks
		long availaBlock = statFs.getAvailableBlocks();
		// long total = totalBlocks * blocSize;
		long availableSpace = availaBlock * blockSize;
		return availableSpace / 1024;
	}
}
