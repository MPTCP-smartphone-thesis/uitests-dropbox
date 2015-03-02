package dropbox;

import utils.Utils;
import android.graphics.Rect;

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
	private static final String ID_BUTTON_QACTION = "com.dropbox.android:id/filelist_quickaction_button";

	private static final String SEND_FILE = "a_random_seed_concat.bin";
	private static int NB_FILES = 2;
	private static int MAX_TIME = 2 * 60;

	private void removePreviousFile(String fileName)
			throws UiObjectNotFoundException {
		// Remove old file
		UiObject oldFile = Utils.findLayoutInList(fileName,
				android.widget.LinearLayout.class.getName(), 2,
				ID_LIST_DROPBOX, ID_TITLE_DROPBOX, true);
		if (oldFile != null && oldFile.exists()) {
			int i;
			// init can take a few time, retry if longPress doesn't work
			for (i = 0; i < 10; i++) {
				// long press for the menu: longClick() doesn't work
				Utils.longPress(oldFile);
				// menu, find delete
				UiObject delete = Utils.findLayoutInList(TEXT_DELETE,
						android.widget.LinearLayout.class.getName(), 0,
						ID_LIST_DIALOG, null, true);
				if (delete != null && delete.exists()) {
					delete.clickAndWaitForNewWindow();
					break;
				}
				sleep(500);

			}
			// confirmation
			if (i != 10) // avoid error
				Utils.clickAndWaitForNewWindow(ID_BUTTON_DELETE);
		}
	}

	private void uploadFile(String fileName) throws UiObjectNotFoundException {
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

	private UiObject waitForEndUpload(String fileName)
			throws UiObjectNotFoundException {
		sleep(5000);
		UiObject uploadingFile = Utils.findLayoutInList(fileName,
				android.widget.FrameLayout.class.getName(), 0, ID_LIST_DROPBOX,
				ID_TITLE_DROPBOX, true);
		for (int i = 5; i < MAX_TIME + 10; i++) {
			UiObject progressBar;
			// Fallback if file not found, find the first ProgressBar
			if (uploadingFile == null || !uploadingFile.exists())
				progressBar = Utils.getObjectWithId(ID_PROGRESSBAR);
			else
				progressBar = uploadingFile.getChild(new UiSelector()
						.resourceId(ID_PROGRESSBAR));
			if (progressBar == null || !progressBar.exists())
				return null;
			sleep(1000);
		}
		return uploadingFile;
	}

	private boolean cancelUpload(UiObject uploadingFile, String fileName)
			throws UiObjectNotFoundException {
		// menu, find cancel
		UiObject fileLayout = Utils.findLayoutInList(fileName,
				android.widget.LinearLayout.class.getName(), 2,
				ID_LIST_DROPBOX, ID_TITLE_DROPBOX, true);
		if (fileLayout != null && fileLayout.exists()) {
			fileLayout.getChild(new UiSelector().resourceId(ID_BUTTON_QACTION))
					.click();
			sleep(1000);
			Rect bounds = fileLayout.getBounds();
			getUiDevice().click(bounds.width() / 2,
					bounds.bottom + bounds.height() / 4);
			return true;
		}
		return false;
	}

	public void testDemo() throws UiObjectNotFoundException {
		assertTrue("OOOOOpps",
				Utils.openApp(this, "Dropbox",
						"com.dropbox.android",
						"com.dropbox.android.activity.DropboxBrowser"));
		double multTime = Utils.getMultTime(this);
		NB_FILES = Math.max(1, (int) (NB_FILES * multTime));
		MAX_TIME = Math.max(1, (int) (MAX_TIME * multTime));
		sleep(1000);

		for (int i = 0; i < NB_FILES; i++) {
			long start = System.currentTimeMillis();
			// create file with a few random
			Utils.createFile(SEND_FILE);
			removePreviousFile(SEND_FILE);

			// upload file and wait
			uploadFile(SEND_FILE);
			UiObject uploadingFile = waitForEndUpload(SEND_FILE);
			if (uploadingFile != null) {
				System.out.println("Timeout, cancel upload");
				// to avoid uploading a file during other tests
				assertTrue("Not able to cancel upload",
						cancelUpload(uploadingFile, SEND_FILE));
				// Error if no file uploaded
				//assertTrue("Not able to upload any file", i > 0);
				System.err.println("WARNING: not enough time to upload a file");
				break;
			}

			// check if we have enough time for a new upload
			int elapsedTimeSec = (int) ((System.currentTimeMillis() - start) / 1000);
			System.out.println("Elapsed time: " + elapsedTimeSec + " - "
					+ MAX_TIME);
			if (MAX_TIME > 2 * elapsedTimeSec)
				MAX_TIME -= elapsedTimeSec;
			else if (i + 1 < NB_FILES) {
				System.out.println("No more time for a new test...");
				break;
			}
		}
	}
}
