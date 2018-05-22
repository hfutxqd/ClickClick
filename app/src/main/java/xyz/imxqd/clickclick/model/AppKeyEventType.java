package xyz.imxqd.clickclick.model;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;

/**
 * Created by imxqd on 2017/11/25.
 */

public enum AppKeyEventType {
    SingleClick {
        @Override
        public String getName() {
            return App.get().getResources().getStringArray(R.array.event_type)[0];
        }
    },
    LongClick {
        @Override
        public String getName() {
            return App.get().getResources().getStringArray(R.array.event_type)[1];
        }
    },
    DoubleClick {
        @Override
        public String getName() {
            return App.get().getResources().getStringArray(R.array.event_type)[2];
        }
    },
    TripleClick{
        @Override
        public String getName() {
            return App.get().getResources().getStringArray(R.array.event_type)[3];
        }
    };
    public abstract String getName();
}
