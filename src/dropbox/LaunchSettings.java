package dropbox;

import utils.Utils;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class LaunchSettings extends UiAutomatorTestCase {

	private static final String ID_LIST_DROPBOX = "android:id/list";
	private static final String ID_TITLE_DROPBOX = "com.dropbox.android:id/filelist_name";
	private static final String TEXT_DELETE = "Delete";
	private static final String ID_LIST_DIALOG = "android:id/select_dialog_listview";
	private static final String ID_BUTTON_DELETE = "android:id/button1";
	private static final String DESC_MORE_OPTIONS = "More options";
	private static final String TEXT_UPLOAD = "Upload here";
	private static final String TEXT_OTHER_FILES = "Other files";
	private static final String ID_LIST_FILES = "com.dropbox.android:id/lfb_list";
	private static final String ID_TITLE_FILE = "com.dropbox.android:id/localitem_name";
	private static final String ID_BUTTON_UPLOAD = "com.dropbox.android:id/bottom_bar_ok_button";
	private static final String ID_PROGRESSBAR = "com.dropbox.android:id/filelist_status_progressbar";

	private static final String SEND_FILE = "random_seed_concat.bin";
	private static final int NB_FILES = 2;
	private static final int MAX_CHECK = 2 * 60;

	private void uploadFile(String fileName) throws UiObjectNotFoundException {
		// Remove old file
		UiObject oldFile = Utils.findLayoutInList(fileName,
				android.widget.LinearLayout.class.getName(), 2,
				ID_LIST_DROPBOX, ID_TITLE_DROPBOX, true);
		if (oldFile != null && oldFile.exists()) {
			// long press for the menu: longClick() doesn't work
			Utils.longPress(oldFile);
			// menu, find delete
			Utils.findLayoutInList(TEXT_DELETE,
					android.widget.LinearLayout.class.getName(), 0,
					ID_LIST_DIALOG, null, true).clickAndWaitForNewWindow();
			// confirmation
			Utils.clickAndWaitForNewWindow(ID_BUTTON_DELETE);
		}

		// Top-right Menu
		Utils.getObjectWithDescription(DESC_MORE_OPTIONS)
				.clickAndWaitForNewWindow();

		// Upload: a list but we need the 2nd item
		Utils.findLayoutWithTitle(TEXT_UPLOAD,
				android.widget.LinearLayout.class.getName(), null, null, 0)
				.clickAndWaitForNewWindow();

		// Other files
		Utils.clickAndWaitForNewWindow(Utils
				.getObjectWithText(TEXT_OTHER_FILES));

		// Find the file
		Utils.findLayoutInList(fileName,
				android.widget.LinearLayout.class.getName(), 3, ID_LIST_FILES,
				ID_TITLE_FILE, true).click();

		// Click on Upload
		Utils.clickAndWaitForNewWindow(ID_BUTTON_UPLOAD);
	}

	private boolean waitForEndUpload(String fileName)
			throws UiObjectNotFoundException {
		UiObject uploadingFile = Utils.findLayoutInList(fileName,
				android.widget.FrameLayout.class.getName(), 0, ID_LIST_DROPBOX,
				ID_TITLE_DROPBOX, true);
		for (int i = 0; i < MAX_CHECK; i++) {
			UiObject progressBar = uploadingFile.getChild(new UiSelector()
					.resourceId(ID_PROGRESSBAR));
			if (progressBar == null || !progressBar.exists())
				return true;
			sleep(1000);
		}
		return false;
	}

	public void testDemo() throws UiObjectNotFoundException {
		assertTrue("OOOOOpps",
				Utils.openApp(this, "Dropbox",
						"com.dropbox.android",
						"com.dropbox.android.activity.DropboxBrowser"));
		sleep(1000);

		for (int i = 0; i < NB_FILES; i++) {
			Utils.createFile(SEND_FILE);
			uploadFile(SEND_FILE);
			assertTrue("Upload: timeout", waitForEndUpload(SEND_FILE));
		}
	}
}