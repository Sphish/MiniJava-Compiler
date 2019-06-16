package symbol;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Vector;

public class RegAlloc {
	private HashMap<String, FlowGraph> flowGraphHashMap;
	private ProcedureBlock curpBlock;
	private Vector<Integer> curactive;
	private Vector<Liveinterval> curlives;
	private boolean[] curusedR;
	private int curspillIdx;

	public RegAlloc(HashMap<String, FlowGraph> flowGraphHashMap_) {
		flowGraphHashMap = flowGraphHashMap_;
	}

	public void updateInterval() {
		for (FlowGraph fg : flowGraphHashMap.values()) {
			ProcedureBlock pBlock = fg.pBlock;
			for (Entry<Integer, FlowBlock> entry : fg.mBlock.entrySet()) {
				int curLine = entry.getKey();
				if (curLine == 0) {
					continue;
				}
				HashSet<Integer> IN = entry.getValue().In;
				for (Integer integer : IN) {
					if (curLine <= pBlock.tmpMap.get(integer).start) {
						pBlock.tmpMap.get(integer).start = curLine;
					}
					if (curLine >= pBlock.tmpMap.get(integer).end) {
						pBlock.tmpMap.get(integer).end = curLine;
					}
				}
			}
		}
	}

	public void expireOld(int intervalId) {
		Liveinterval curInterval = curlives.get(intervalId);
		int cursize = curactive.size();
		for (int j = 0, realj = 0; j < cursize; j++, realj++) {
			int curj = curactive.get(realj);
			if (curlives.get(curj).end < curInterval.start) {
				curactive.remove(realj);
				realj -= 1;
				int curTmpNum = curlives.get(curj).tmpnum;
				if (curpBlock.regCandi.containsKey(curTmpNum)) {
					int usedReg = Integer.parseInt(curpBlock.regCandi.get(curTmpNum));
					curusedR[usedReg] = false;
				}
			}
		}
	}

	public void spillInterval(int intervalId) {
		Liveinterval spill = curlives.get(curactive.get(0));
		int spill_idx = 0;
		int cur_idx = 0;
		for (int i : curactive) {
			Liveinterval mapInterval = curlives.get(i);
			if (mapInterval.end > spill.end) {
				spill = mapInterval;
				spill_idx = cur_idx;
			}
			cur_idx++;
		}
		Liveinterval curInterval = curlives.get(intervalId);
		if (spill.end > curInterval.end) {
			curpBlock.regCandi.put(curInterval.tmpnum, curpBlock.regCandi.get(spill.tmpnum));
			curpBlock.regCandi.remove(spill.tmpnum);
			curpBlock.regStack.put(spill.tmpnum, "SPILLEDARG " + curspillIdx);
			curactive.remove(spill_idx);
			curactive.add(intervalId);
		} else {
			curpBlock.regStack.put(curInterval.tmpnum, "SPILLEDARG " + curspillIdx);
		}
		curspillIdx++;
	}

	public void alloc() {
		updateInterval();
		for (FlowGraph fg : flowGraphHashMap.values()) {
			Vector<Liveinterval> lives = new Vector<Liveinterval>();
			curpBlock = fg.pBlock;
			curlives = lives;
			for (Liveinterval liint : curpBlock.tmpMap.values()) {
				curlives.add(liint);
			}
			Collections.sort(curlives);

			Vector<Integer> active = new Vector<Integer>();
			curactive = active;
			int curIntervalnum = 0;
			curspillIdx = 0;
			if (curpBlock.paranum > 4) {
				curspillIdx = curpBlock.paranum - 4;
			}
			int R = 18;
			boolean[] usedR = new boolean[R];
			curusedR = usedR;
			for (Liveinterval liint : curlives) {
				if (liint.start == liint.end) {
					curpBlock.regSkip.add(liint.tmpnum);
					curIntervalnum++;
					continue;
				}
				expireOld(curIntervalnum);
				if (curactive.size() == R) {
					spillInterval(curIntervalnum);
				} else {
					for (int r = 0; r < R; ++r) {
						if (!curusedR[r]) {
							curusedR[r] = true;
							curpBlock.regCandi.put(liint.tmpnum, "" + r);
							break;
						}
					}
					curactive.add(curIntervalnum);
				}
				curIntervalnum++;
			}
			HashMap<Integer, String> regCandi = new HashMap<Integer, String>();
			for (Entry<Integer, String> entry : fg.pBlock.regCandi.entrySet()) {
				int r = Integer.parseInt(entry.getValue());
				String regname;
				if (r < 10) {
					regname = "t" + r;
				} else {
					regname = "s" + (r - 10);
				}
				regCandi.put(entry.getKey(), regname);
			}
			curpBlock.regCandi = regCandi;
			for (String regname : curpBlock.regCandi.values()) {
				if (!curpBlock.regSave.contains(regname)) {
					curpBlock.regSave.add(regname);
				}
			}
			curpBlock.useStack = curspillIdx + curpBlock.regSave.size();
		}
	}
}
