package test.com.uidraft.abstractus;

//import android.app.Fragment;

/**
 * Created by user on 24.05.15.
 */
public class DF {
    public abstract static class DFObject {

        public abstract int getID();

    }

    public static class FlashModeChanged extends DFObject {
        public int mode;

        public FlashModeChanged() {
        }

        public FlashModeChanged(int mode) {
            this.mode = mode;
        }

        public static final int ID = 1;

        @Override
        public int getID() {
            return 1;
        }
    }

    public static class OrientationChanged extends DFObject {
        public int newOrientation;

        public OrientationChanged() {
        }

        public OrientationChanged(int newOrientation) {
            this.newOrientation = newOrientation;
        }

        public static final int ID = 0;

        @Override
        public int getID() {
            return 0;
        }
    }

    public static class FragmentIsReady extends DFObject {
        public int fid;
        public int viewId;
        public FragmentIsReady() {
        }

        public FragmentIsReady(int fid, int viewId) {
            this.fid = fid;
            this.viewId = viewId;
        }

        public static final int ID = 80;

        @Override
        public int getID() {
            return 80;
        }
    }



    public static class FragmentAttached extends DFObject {
        public int fid;

        public FragmentAttached() {
        }

        public FragmentAttached(int fid) {
            this.fid = fid;
        }

        public static final int ID = 81;

        @Override
        public int getID() {
            return 81;
        }
    }
}
