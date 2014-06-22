package com.cpx.audio.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cpx.audio.R;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

	static {
		// Add 3 sample items.
		addItem(new DummyItem("1", "¼��", R.drawable.icon_luyin));
		addItem(new DummyItem("2", "����", R.drawable.icon_bofang));
		addItem(new DummyItem("3", "����", R.drawable.icon_shezhi));
	}

	private static void addItem(DummyItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class DummyItem {
		public String id;
		public String content;
		public int drawableid;

		public DummyItem(String id, String content, int drawableid) {
			this.id = id;
			this.content = content;
			this.drawableid = drawableid;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
