package xyz.imxqd.clickclick.log;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.json.JSONException;

public class ClickSender implements ReportSender{
    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException {
        Toast.makeText(context, "I'm sorry. ClickClick crashed. Please send the crash log to me.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TITLE, "ClickClick crash log");
        try {
            intent.putExtra(Intent.EXTRA_TEXT, errorContent.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        context.startActivity(intent);
    }
}
