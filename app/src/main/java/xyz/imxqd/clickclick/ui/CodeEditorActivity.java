package xyz.imxqd.clickclick.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.imxqd.clickclick.R;

public class CodeEditorActivity extends AppCompatActivity {

    @BindView(R.id.action_title)
    TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_editor);
        ButterKnife.bind(this);
        mTitle.setText(getTitle());
    }
}
