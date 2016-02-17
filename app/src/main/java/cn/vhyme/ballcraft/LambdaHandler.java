package cn.vhyme.ballcraft;

import android.os.Handler;
import android.os.Message;

public class LambdaHandler extends Handler {

    Runnable mRunnable = null;

    public LambdaHandler(Runnable runnable) {
        mRunnable = runnable;
    }

    @Override
    public void handleMessage(Message msg) {
        if(mRunnable != null) mRunnable.run();
    }

    public void run(){
        sendMessage(new Message());
    }
}
