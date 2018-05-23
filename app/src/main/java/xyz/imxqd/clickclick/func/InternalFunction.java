package xyz.imxqd.clickclick.func;

import android.content.Intent;

import xyz.imxqd.clickclick.App;

public class InternalFunction extends AbstractFunction {
    public static final String PREFIX = "internal";

    public InternalFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) {

    }

    public void openCarmera() {
        Intent intent = new Intent("android.media.action.STILL_IMAGE_CAMERA");
        App.get().startActivity(intent);
    }

    public void openCarmeraInVideoMode() {
        Intent intent = new Intent("android.media.action.VIDEO_CAMERA");
        App.get().startActivity(intent);
    }
    
    public void cloudMusicLike() {
        // TODO: 2018/5/23  
    }

    public void qqMusicLike() {
        // TODO: 2018/5/23
    }
}
