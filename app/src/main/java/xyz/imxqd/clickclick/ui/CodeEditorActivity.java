package xyz.imxqd.clickclick.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.ahmadaghazadeh.editor.widget.CodeEditor;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.vimfung.luascriptcore.LuaContext;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.luaframework.LuaEngine;

public class CodeEditorActivity extends AppCompatActivity {

    @BindView(R.id.code_editor)
    CodeEditor codeEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_editor);
        ButterKnife.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.code_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_run) {
            String code = codeEditor.getCode();
            LuaEngine.createContext().evalScript(code);
        } else if (item.getItemId() == R.id.action_save) {

        }
        return true;
    }
}
